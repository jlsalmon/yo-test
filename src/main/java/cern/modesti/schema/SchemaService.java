package cern.modesti.schema;

import java.util.ArrayList;
import java.util.List;

import cern.modesti.repository.mongo.schema.SchemaRepository;
import cern.modesti.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.schema.field.Field;

@Service
public class SchemaService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);

  @Autowired
  private SchemaRepository schemaRepository;

  /**
   * TODO rewrite this and explain it better
   *
   * The schema hierarchy looks like the following:
   *
   *
   * core                                                                  [core]
   * |
   * +--------------------------+-----------------------------------+
   * |                          |                                   |
   * tim                        csam                               pvss    [domains]
   * |                          |                                   |
   * +-----+-----+-----+        +--------+--------+--------+        +
   * |     |     |     |        |        |        |        |        |
   * plc   opc   japc  ...      lsac   winter  securiton   ...     ...     [categories]
   *
   * Each domain has a schema which inherits from the core schema. Each domain has a number of associated category schemas which inherit from their domain schema. When a schema
   * is materialised, it is merged its parents, and optionally its siblings if they were specified.
   *
   * Note: it is not possible to merge a category from another domain into a schema.
   *
   *
   * @param request
   * @param categories
   *
   * @return
   */
  Schema materialiseSchema(Request request, List<String> categories) {
    Schema schema = new Schema(request.getRequestId(), request.getDescription(), request.getDomain());

    // Merge all sibling schemas
    for (String category : categories) {
      LOG.info("finding and merging schema for category " + category);

      Schema categorySchema = schemaRepository.findOneByNameIgnoreCase(category);
      if (categorySchema == null) {
        throw new IllegalStateException("Schema for category \"" + category + "\" was not found");
      }

      schema = mergeSiblingSchema(schema, categorySchema);
    }

    // Merge the domain schema
    Schema domainSchema = schemaRepository.findOneByNameIgnoreCase(request.getDomain());
    if (domainSchema == null) {
      throw new IllegalStateException("Schema for domain \"" + request.getDomain() + "\" was not found");
    }
    schema = mergeDomainSchema(schema, domainSchema);

    // Merge the core schema
    Schema parent = schemaRepository.findOneByNameIgnoreCase(domainSchema.getParent());
    if (parent == null) {
      throw new IllegalStateException("Parent schema \"" + domainSchema.getParent() + "\" for domain " + domainSchema.getName() + " was not found");
    }
    schema = mergeParentSchema(schema, parent);

    return schema;


//    for (String category : categories) {
//      Schema categorySchema = schemaRepository.findOneByName(category);
//
//      if (categorySchema == null) {
//        return categorySchema;
//      }
//
//      // Merge in the domain schema and all parent schemas.
//      //
//      // The merged schema is not saved back to the repository, it is generated
//      // fresh each time. This is because we don't want to duplicate the
//      // categories/fields defined in the parent schemas, and because it makes
//      // editing schemas cleaner.
//
//      Schema domain = schemaRepository.findOneByName(categorySchema.getDomain());
//      if (domain == null) {
//        throw new IllegalStateException("Schema \"" + categorySchema.getName() + "\" specifies non-existent domain schema \"" + categorySchema.getDomain() + "\"");
//      }
//
//      categorySchema = mergeDomainSchema(categorySchema, domain);
//
//      Schema parent = schemaRepository.findOneByName(domain.getParent());
//      if (parent == null) {
//        throw new IllegalStateException("Schema \"" + categorySchema.getName() + "\" specifies non-existent parent schema \"" + categorySchema.getParent() + "\"");
//      }
//
//      categorySchema = mergeParentSchema(categorySchema, parent);
//    }
//
//    return null;
  }

  /**
   *
   * @param schema
   * @param sibling
   * @return
   */
  Schema mergeSiblingSchema(Schema schema, Schema sibling) {
    List<Category> categories = schema.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category siblingCategory : sibling.getCategories()) {
      if (!categories.contains(siblingCategory)) {
        newCategories.add(siblingCategory);

      } else {
        Category category = categories.get(categories.indexOf(siblingCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field siblingField : siblingCategory.getFields()) {
          if (!category.getFields().contains(siblingField)) {
            newFields.add(siblingField);
          }
        }

        newFields.addAll(category.getFields());
        category.setFields(newFields);
      }
    }

    newCategories.addAll(schema.getCategories());
    schema.setCategories(newCategories);

    return schema;
  }

  /**
   * @param schema
   * @param domain
   *
   * @return
   */
  Schema mergeDomainSchema(Schema schema, Schema domain) {
    List<Category> categories = schema.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category domainCategory : domain.getCategories()) {
      if (!categories.contains(domainCategory)) {
        newCategories.add(domainCategory);

      } else {
        Category category = categories.get(categories.indexOf(domainCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field domainField : domainCategory.getFields()) {
          if (!category.getFields().contains(domainField)) {
            newFields.add(domainField);
          }
        }

        newFields.addAll(category.getFields());
        category.setFields(newFields);
      }
    }

    newCategories.addAll(schema.getCategories());
    schema.setCategories(newCategories);

    return schema;
  }

  /**
   * @param schema
   * @param parent
   *
   * @return
   */
  Schema mergeParentSchema(Schema schema, Schema parent) {
    List<Category> categories = schema.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category parentCategory : parent.getCategories()) {
      if (!categories.contains(parentCategory)) {
        newCategories.add(parentCategory);

      } else {
        Category category = categories.get(categories.indexOf(parentCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field parentField : parentCategory.getFields()) {
          if (!category.getFields().contains(parentField)) {
            newFields.add(parentField);
          }
        }

        newFields.addAll(category.getFields());
        category.setFields(newFields);
      }
    }

    newCategories.addAll(schema.getCategories());
    schema.setCategories(newCategories);

    return schema;
  }
}
