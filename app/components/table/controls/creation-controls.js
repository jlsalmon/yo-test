'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationControlsController
 * @description # CreationControlsController Controller of the modesti
 */
angular.module('modesti').controller('CreationControlsController', CreationControlsController);

function CreationControlsController($http, $state, $timeout, $modal, RequestService, TaskService, ValidationService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.rows = {};
  self.tasks = {};
  self.hot = {};

  self.validating = undefined;
  self.submitting = undefined;
  self.splitting = undefined;

  self.init = init;
  self.validate = validate;
  self.submit = submit;
  self.split = split;
  self.canValidate = canValidate;
  self.canSubmit = canSubmit;
  self.canSplit = canSplit;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
    self.request = parent.request;
    self.rows = parent.rows;
    self.tasks = parent.tasks;
    self.hot = parent.hot;


    // Register the afterChange() hook so that we can use it to send a signal to the backend if we are in 'submit'
    // state and the user makes a modification
    self.hot.addHook('afterChange', afterChange);

    // Update the table settings to paint the row backgrounds depending on if they have already been approved
    // or rejected
    if (self.request.approvalResult) {
      self.hot.updateSettings({
        cells: function (row, col, prop) {
          if (self.request.approvalResult.items[row].approved == false) {
            return {renderer: self.parent.dangerCellRenderer};
          }
        }
      });
    }
  }

  /**
   *
   */
  function canValidate() {
    var task = self.tasks['validate'];
    return task;
  }

  /**
   *
   */
  function canSubmit() {
    return self.tasks['submit'];
  }

  /**
   *
   */
  function canSplit() {
    return self.parent.getNumValidationErrors() > 0;
    //return self.parent.getSelectedPointIds().length > 0;
  }

  /**
   *
   */
  function validate() {
    self.validating = 'started';

    $timeout(function () {
      ValidationService.validateRequest(self.rows, self.parent.schema).then(function (valid) {
        // Render the table to show the error highlights
        self.hot.render();

        if (!valid) {
          self.validating = 'error';
          return;
        }

        // Validate server-side
        var task = self.tasks['validate'];

        if (!task) {
          console.log('warning: no validate task found');
          return;
        }

        // First save the request
        RequestService.saveRequest(self.request).then(function () {
          console.log('saved request before validation');

          // Complete the task associated with the request
          TaskService.completeTask(task.id).then(function (task) {
            console.log('completed task ' + task.id);

            // Clear the cache so that the state reload also pulls a fresh request
            RequestService.clearCache();

            $state.reload().then(function () {
              self.validating = 'success';
            });
          },

          function (error) {
            console.log('error completing task: ' + error.statusText);
            self.validating = 'error';
          });
        },

        function (error) {
          console.log('error saving before validation: ' + error.statusText);
          self.validating = 'error';
        });
      });

    })
  }

  /**
   *
   */
  function submit() {
    var task = self.tasks['submit'];

    if (!task) {
      console.log('warning: no submit task found');
      return;
    }

    self.submitting = 'started';

    // Complete the task associated with the request
    TaskService.completeTask(task.id).then(function (task) {
      console.log('completed task ' + task.id);

      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();

      $state.reload().then(function () {
        self.submitting = 'success';
      });
    },

    function (error) {
      console.log('error completing task: ' + error);
      self.submitting = 'error';
    });
  }

  /**
   *
   */
  function split() {
    var task = self.tasks['validate'];
    if (!task) {
      console.log('error splitting request: no task');
      return;
    }

    var selectedPointIds = self.parent.getSelectedPointIds();

    if (!selectedPointIds.length) {
      return;
    }

    console.log('splitting points: ' + selectedPointIds);

    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/table/controls/modals/splitting-modal.html',
      controller: 'SplittingModalController as ctrl',
      resolve: {
        selectedPointIds: function () {
          return selectedPointIds;
        },
        rows: function () {
          return self.rows;
        }
      }
    });

    // Callback fired when the user clicks 'ok'. Not fired if 'cancel' clicked.
    modalInstance.result.then(function () {
      self.splitting = 'started';

      var url = task.executionUrl;
      var variables = [{
        "name": "points",
        "value": JSON.stringify(selectedPointIds),
        "type": "string"
      }];

      var params = {
        "action": "signalEventReceived",
        "signalName": "splitRequest",
        "variables": variables
      };

      // TODO refactor this into a service
      $http.put(url, params).then(function () {
        console.log('sent split signal');

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.splitting = 'success';
        });
      },

      function (error) {
        console.log('error sending signal: ' + error);
        self.splitting = 'error';
      });
    });
  }

  /**
   * Called after a change is made to the table (edit, paste, etc.)
   *
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function afterChange(changes, source) {
    console.log('afterChange()');

    // When the table is initially loaded, this callback is invoked with source == 'loadData'. In that case, we don't
    // want to save the request or send the modification signal.
    if (source == 'loadData') {
      return;
    }

    if (source == 'paste') {
      console.log('paste');
      return;
    }

    // Make sure the point IDs are consecutive
    for (var i = 0, len = self.rows.length; i < len; i++) {
      self.rows[i].id = i + 1;
    }

    // Loop over the changes and check if anything actually changed. Mark any changed points as dirty.
    var change, index, property, oldValue, newValue, dirty = false;
    for (var i = 0, len = changes.length; i < len; i++) {
      change = changes[i];
      index = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      // Mark the point as dirty.
      if (newValue != oldValue) {
        console.log('dirty point: ' + self.rows[index].id);
        dirty = true;
        self.rows[index].dirty = true;
      }
    }

    // If nothing changed, there's nothing to do! Otherwise, save the request.
    if (dirty) {
      RequestService.saveRequest(self.request).then(function () {
        // If we are in the "submit" stage of the workflow and the form is modified, then it will need to be
        // revalidated. This is done by sending the "requestModified" signal.
        if (self.tasks['submit']) {
          sendModificationSignal();
        }
      });
    }
  }

  /**
   * Sends the "requestModified" signal when in the "submit" stage of the workflow in order to force the request
   * back to the "validate" stage.
   */
  function sendModificationSignal() {
    var task = self.tasks['submit'];
    if (task) {
      console.log('form modified whilst in submit state: sending signal');

      var url = task.executionUrl;
      var params = {
        "action": "signalEventReceived",
        "signalName": "requestModified",
        "variables": []
      };

      // TODO refactor this into a service
      $http.put(url, params).then(function () {
        console.log('sent modification signal');

        // The "submit" task will have changed to "validate".
        TaskService.queryTasksForRequest(self.request).then(function (tasks) {
          self.tasks = tasks;
        });
      },

      function (error) {
        console.log('error sending signal: ' + error);
      });
    }
  }
}