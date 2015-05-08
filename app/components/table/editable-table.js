'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:EditableTableController
 * @description # EditableTableController Controller of the modesti
 */
angular.module('modesti').controller('EditableTableController', EditableTableController);

function EditableTableController($scope, $http, $stateParams, NgTableParams, RequestService, ValidationService) {
  var self = this;

  self.request = {};
  self.schema = {};
  self.searchText = {};

  self.tableParams = new NgTableParams({
    page : 1,
    count : 10,
    sorting : {id : 'asc'}
  }, {
    total : 0,
    filterDelay : 0,
    $scope: self, // see https://github.com/esvit/ng-table/issues/362
    getData : getTableData
  });

  self.checkboxes = {
    'checked' : false,
    items : {}
  };

  self.availableCategories = [];

  self.init = init;
  self.activateCategory = activateCategory;
  self.addNewCategory = addNewCategory;
  self.getAvailableCategories = getAvailableCategories;
  self.getActiveFields = getActiveFields;
  self.addRow = addRow;
  self.duplicateSelectedRows = duplicateSelectedRows;
  self.deleteSelectedRows = deleteSelectedRows;
  self.save = save;
  self.validate = validate;
  self.toggleFilter = toggleFilter;
  self.getSortingClass = getSortingClass;
  self.toggleSorting = toggleSorting;

  /**
   *
   * @param request
   * @param schema
   */
  function init(request, schema) {
    self.request = request;
    self.schema = schema;
    getAvailableCategories();
  }

  /**
   *
   * @param $defer
   * @param params
   */
  function getTableData($defer, params) {
    console.log('getting table data');
    var id = $stateParams.id;

    // If we already have a request, send it to the service for merging,
    // as we might have made unsaved changes.
    var unsavedRequest = self.request ? self.request : undefined;

    RequestService.getRequest(id, params, unsavedRequest).then(function(request) {
      self.request = request;
      console.log('got request (with ' + request.points.length + ' points)');

      // Set total for pagination
      params.total(request.points.length);

      // Slice the points into pages and resolve the promise
      $defer.resolve(request.points.slice((params.page() - 1) * params.count(), params.page() * params.count()));
    },

    function(error) {
      console.log('error getting request: ' + error);
    });
  }

  /**
   *
   */
  function activateCategory(category) {
    console.log('activating category');
    var categories = self.schema.categories;

    for (var key in categories) {
      if (categories.hasOwnProperty(key)) {
        categories[key].active = false;
        console.log('category:' + categories[key]);
      }
    }

    category.active = true;
    console.log(category);
    self.activeFields = getActiveFields();
  }

  /**
   *
   * @param category
   */
  function addNewCategory(category) {
    console.log("adding category " + category);

    var schemaLink = self.request._links.schema.href;

    if(schemaLink.indexOf('?categories') > -1) {
      schemaLink += ',' + category;
    } else {
      schemaLink += '?categories=' + category;
    }

    // TODO refactor this into a service
    $http.get(schemaLink).then(function(response) {
      console.log('fetched new schema: ' + response.data.name);
      self.schema = response.data;
      self.request._links.schema.href = schemaLink;
      getAvailableCategories();
    },

    function(error) {
      console.log('error fetching schema: ' + error);
    });
  }

  /**
   *
   */
  function getAvailableCategories() {
    // TODO refactor this into a service
    $http.get('http://localhost:8080/domains/' + self.request.domain).then(function(response) {
      self.availableCategories = [];

      response.data.datasources.map(function(category) {

        // Only add the category if we aren't already using it
        if ($.grep(self.schema.categories, function(item){ return item.name == category.name; }).length == 0) {
          self.availableCategories.push(category.name);
        }
      });
    });
  }

  /**
   *
   */
  function getActiveFields() {
    var categories = self.schema.categories;
    var fields = [];

    for ( var key in categories) {
      if (categories.hasOwnProperty(key)) {
        var category = categories[key];

        if (category.active) {
          fields = category.fields;
        }
      }
    }

    console.log('got active fields');
    return fields;
  }

  /**
   *
   */
  function addRow() {
    console.log('adding new row');
    var request = self.request;

    var newRow = {
      'name' : '',
      'description' : '',
      'domain' : request.domain
    };

    request.points.push(newRow);

    RequestService.saveRequest(request).then(function(request) {
      console.log('added new row');
      self.request = request;

      // Reload the table data
      self.tableParams.reload();

      // Move to the last page
      var pages = self.tableParams.settings().$scope.pages;
      for (var i in pages) {
        if (pages[i].type == "last") {
          self.tableParams.page(pages[i].number);
        }
      }

    }, function(error) {
      console.log('error adding new row: ' + error);
    });
  }

  /**
   *
   */
  function duplicateSelectedRows() {
    var points = self.request.points;
    console.log('duplicating rows (before: ' + points.length + ' points)');

    // Find the selected points and duplicate them
    for (var i in points) {
      var point = points[i];

      if (self.checkboxes.items[point.id]) {
        var duplicate = angular.copy(point);
        // Remove the ID of the duplicated point, as the backend will generate
        // us a new one when we save
        delete duplicate.id;
        // Add the new duplicate to the original points
        points.push(duplicate);
      }
    }

    // Save the changes
    RequestService.saveRequest(self.request).then(function(savedRequest) {
      console.log('saved request after row duplication');
      console.log('duplicated rows (after: ' + savedRequest.points.length + ' points)');

      // Reload the table data
      self.tableParams.reload();

    }, function(error) {
      console.log('error saving request after row duplication: ' + error);
    });
  }

  /**
   *
   */
  function deleteSelectedRows() {
    var points = self.request.points;
    console.log('deleting rows (before: ' + points.length + ' points)');

    // Find the selected points and mark them as deleted
    for (var i in points) {
      var point = points[i];

      if (self.checkboxes.items[point.id]) {
        point.deleted = true;
      }
    }

    // Save the changes
    RequestService.saveRequest(self.request).then(function(savedRequest) {
      console.log('saved request after row deletion');
      console.log('deleted rows (after: ' + savedRequest.points.length + ' points)');

      // Reload the table data
      self.tableParams.reload();

    }, function(error) {
      console.log('error saving request after row deletion: ' + error);
    });
  }

  /**
   *
   */
  function save() {
    var request = self.request;

    RequestService.saveRequest(request).then(function() {
      console.log('saved request');
    }, function(error) {
      console.log('error saving request');
    });
  }

  /**
   *
   */
  function validate() {
    var request = self.request;

    ValidationService.validateRequest(request).then(function(result) {
      console.log('validated request');
      self.validationResult = result;
    }, function(error) {
      console.log('error validating request: ' + error);
    });
  }

  /**
   *
   */
  function toggleFilter() {
    self.tableParams.settings().$scope.show_filter = !self.tableParams.settings().$scope.show_filter;
  }

  /**
   *
   * @param property
   */
  function getSortingClass(property) {
    if (self.tableParams.isSortBy(property, 'asc')) return 'sort-asc';
    if (self.tableParams.isSortBy(property, 'desc')) return 'sort-desc';
  }

  /**
   *
   * @param property
   */
  function toggleSorting(property) {
    self.tableParams.sorting(property, self.tableParams.isSortBy(property, 'asc') ? 'desc' : 'asc');
  }

  // TODO: remove these watches and use ng-change instead

  $scope.$watch("ctrl.searchText", function() {
    if (!jQuery.isEmptyObject(self.searchText) && self.tableParams) {
      self.tableParams.filter({properties: self.searchText});
    }
  }, true);

  // watch for check all checkbox
  $scope.$watch('ctrl.checkboxes.checked', function(value) {
    if (!self.request) {
      return;
    }

    angular.forEach(self.request.points, function(point) {
      if (angular.isDefined(point.id)) {
        self.checkboxes.items[point.id] = value;
      }
    });
  });

  // watch for data checkboxes
  $scope.$watch('ctrl.checkboxes.items', function(values) {
    if (!self.request) {
      return;
    }

    var checked = 0, unchecked = 0, total = self.request.points.length;
    angular.forEach(self.request.points, function(point) {
      checked += (self.checkboxes.items[point.id]) || 0;
      unchecked += (!self.checkboxes.items[point.id]) || 0;
    });

    if ((unchecked == 0) || (checked == 0)) {
      self.checkboxes.checked = (checked == total);
    }

    self.checkboxes.dirty = checked != 0 ? true : false;

    // greyed checkbox
    angular.element(document.getElementById("select_all")).prop("indeterminate", (checked != 0 && unchecked != 0));
  }, true);
}