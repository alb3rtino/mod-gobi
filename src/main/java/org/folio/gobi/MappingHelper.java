package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.gobi.Mapper.Translation;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.Mapping.Field;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.mappings.model.OrderMappings.OrderType;
import org.w3c.dom.NodeList;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingHelper {
  private static final Logger logger = LogManager.getLogger(MappingHelper.class);

  private MappingHelper() {
    throw new IllegalStateException("MappingHelper class cannot be instantiated");
  }

  private static final Map<OrderMappings.OrderType, OrderMappings> defaultMappings = new EnumMap<>(
      OrderMappings.OrderType.class);

  static {
    for (OrderMappings.OrderType orderType : OrderMappings.OrderType.values()) {
      defaultMappings.put(orderType, Json.decodeValue(readMappingsFile(orderType.toString() + ".json"), OrderMappings.class));
    }
  }

  public static Map<Mapping.Field, DataSourceResolver> getDefaultMappingForOrderType(
      PostGobiOrdersHelper postGobiOrdersHelper, OrderType orderType) {
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping = new EnumMap<>(Mapping.Field.class);
    List<Mapping> mappingsList = defaultMappings.get(orderType).getMappings();

    for (Mapping mapping : mappingsList) {
      Mapping.Field field = mapping.getField();
      org.folio.gobi.DataSourceResolver dataSource = getDS(mapping, fieldDataSourceMapping, postGobiOrdersHelper);
      fieldDataSourceMapping.put(field, dataSource);
    }

    return fieldDataSourceMapping;
  }

  public static Object getDefaultValue(org.folio.rest.mappings.model.DataSource dataSource,
      Map<Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping, PostGobiOrdersHelper postGobiOrdersHelper) {
    Object ret = null;
    if (dataSource.getDefault() != null) {
      ret = dataSource.getDefault();
    } else if (dataSource.getFromOtherField() != null) {
      String otherField = dataSource.getFromOtherField().value();
      ret = fieldDataSourceMapping.get(Field.valueOf(otherField));
    } else if (dataSource.getDefaultMapping() != null) {
      ret = getDS(dataSource.getDefaultMapping(), fieldDataSourceMapping, postGobiOrdersHelper);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static org.folio.gobi.DataSourceResolver getDS(Mapping mapping,
      Map<Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping, PostGobiOrdersHelper postGobiOrdersHelper) {

    Object defaultValue = getDefaultValue(mapping.getDataSource(), fieldDataSourceMapping, postGobiOrdersHelper);
    Boolean translateDefault = mapping.getDataSource().getTranslateDefault();
    String dataSourceFrom = mapping.getDataSource().getFrom();

    org.folio.rest.mappings.model.DataSource.Combinator combinator = mapping.getDataSource().getCombinator();
    NodeCombinator nc = null;
    if (combinator != null) {
      try {
        Method combinatorMethod = Mapper.class.getMethod(combinator.toString(), NodeList.class);
        nc = data -> {
          try {
            return (String) combinatorMethod.invoke(null, data);
          } catch (Exception e) {
            String errorMessage = String.format("Unable to invoke combinator method: %s", combinator);
            logger.error(errorMessage, e);
          }
          return null;
        };
      } catch (NoSuchMethodException e) {
        String errorMessage = String.format("Combinator method not found: %s", combinator);
        logger.error(errorMessage, e);
      }
    }

    org.folio.rest.mappings.model.DataSource.Translation translation = mapping.getDataSource().getTranslation();
    Translation<?> t = null;
    if (translation != null) {

      t = data -> {
        Object translatedValue;
        try {
          switch (translation) {
          case LOOKUP_MOCK:
            translatedValue = postGobiOrdersHelper.lookupMock(data);
            break;
          case LOOKUP_CONTRIBUTOR_NAME_TYPE_ID:
            translatedValue = postGobiOrdersHelper.lookupContributorNameTypeId(data);
            break;
          case LOOKUP_EXPENSE_CLASS_ID:
            translatedValue = postGobiOrdersHelper.lookupExpenseClassId(data);
            break;
          case LOOKUP_ACQUISITION_METHOD_IDS:
            translatedValue = postGobiOrdersHelper.lookupAcquisitionMethodId(data);
            break;
          case LOOKUP_LOCATION_ID:
            translatedValue = postGobiOrdersHelper.lookupLocationId(data);
            break;
          case LOOKUP_MATERIAL_TYPE_ID:
            translatedValue = postGobiOrdersHelper.lookupMaterialTypeId(data);
            break;
          case LOOKUP_FUND_ID:
            translatedValue = postGobiOrdersHelper.lookupFundId(data);
            break;
          case LOOKUP_ORGANIZATION:
            translatedValue = postGobiOrdersHelper.lookupOrganization(data);
            break;
          case LOOKUP_PRODUCT_ID_TYPE:
            translatedValue = postGobiOrdersHelper.lookupProductIdType(data);
            break;
          case SEPARATE_ISBN_QUALIFIER:
            translatedValue = postGobiOrdersHelper.separateISBNQualifier(data);
            break;
          case TRUNCATE_ISBN_QUALIFIER:
            translatedValue = postGobiOrdersHelper.truncateISBNQualifier(data);
            break;
          case TO_DATE:
            translatedValue = Mapper.toDate(data);
            break;
          case TO_DOUBLE:
            translatedValue = Mapper.toDouble(data);
            break;
          case TO_INTEGER:
            translatedValue = Mapper.toInteger(data);
            break;
          case TO_BOOLEAN:
            translatedValue = Mapper.toBoolean(data);
            break;
          default:
            throw new IllegalArgumentException("No such Translation available: " + translation);
          }
          return (CompletableFuture<Object>) translatedValue;
        } catch (Exception e) {
          logger.error("Exception in Mapperhelper", e);
        }
        return null;
      };
    }

    return org.folio.gobi.DataSourceResolver.builder()
        .withFrom(dataSourceFrom)
        .withDefault(defaultValue)
        .withTranslation(t)
        .withTranslateDefault(translateDefault != null && translateDefault)
        .withCombinator(nc)
        .build();

  }

  public static Map<Mapping.Field, DataSourceResolver> extractOrderMappings(OrderMappings.OrderType orderType,
      JsonObject jo, PostGobiOrdersHelper postGobiOrdersHelper) {
    final Map<Mapping.Field, DataSourceResolver> map = new EnumMap<>(Mapping.Field.class);

    final JsonArray configs = jo.getJsonArray("configs");

    if (!configs.isEmpty()) {
      final String mappingsString = configs.getJsonObject(0).getString("value");
      final OrderMappings orderMapping = Json.decodeValue(mappingsString, OrderMappings.class);

      final List<Mapping> orderMappingList = orderMapping.getMappings();

      if (orderMappingList != null) {
        for (Mapping mapping : orderMappingList) {
          logger.info("Mapping exists for type: {} , field: {}", orderType, mapping.getField());
          map.put(mapping.getField(), getDS(mapping, map, postGobiOrdersHelper));
        }
      }
    }
    return map;
  }

  public static String readMappingsFile(final String path) {
    try (InputStream is = PostGobiOrdersHelper.class.getClassLoader().getResourceAsStream(path)) {
      if (is != null) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      String errorMessage = String.format("Unable to read configuration in file: %s", path);
      logger.error(errorMessage, e);
    }
    return StringUtils.EMPTY;
  }
}
