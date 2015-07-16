/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.materialize;

import org.apache.calcite.jdbc.CalciteRootSchema;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.type.RelDataType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

/**
 * Actor that manages the state of materializations in the system.
 */
class MaterializationActor {
  // Not an actor yet -- TODO make members private and add request/response
  // queues

  final Map<MaterializationKey, Materialization> keyMap = Maps.newHashMap();

  final Map<MaterializationKey, Prepare.Materialization> keyPrepareMap = Maps.newHashMap();

  final Map<QueryKey, MaterializationKey> keyBySql = Maps.newHashMap();

  final Map<TileKey, MaterializationKey> keyByTile = Maps.newHashMap();

  /** Tiles grouped by dimensionality. We use a
   *  {@link TileKey} with no measures to represent a
   *  dimensionality. */
  final Multimap<TileKey, TileKey> tilesByDimensionality =
      HashMultimap.create();

  /** A query materialized in a table, so that reading from the table gives the
   * same results as executing the query. */
  static class Materialization {
    final MaterializationKey key;
    final CalciteRootSchema rootSchema;
    CalciteSchema.TableEntry materializedTable;
    final String sql;
    final RelDataType rowType;

    /** Creates a materialization.
     *
     * @param key  Unique identifier of this materialization
     * @param materializedTable Table that currently materializes the query.
     *                          That is, executing "select * from table" will
     *                          give the same results as executing the query.
     *                          May be null when the materialization is created;
     *                          materialization service will change the value as
     * @param sql  Query that is materialized
     * @param rowType Row type
     */
    Materialization(MaterializationKey key,
        CalciteRootSchema rootSchema,
        CalciteSchema.TableEntry materializedTable,
        String sql,
        RelDataType rowType) {
      this.key = key;
      this.rootSchema = rootSchema;
      this.materializedTable = materializedTable; // may be null
      this.sql = sql;
      this.rowType = rowType;
    }
  }

  /** A materialization can be re-used if it is the same SQL, on the same
   * schema, with the same path for resolving functions. */
  static class QueryKey {
    final String sql;
    final CalciteSchema schema;
    final List<String> path;

    QueryKey(String sql, CalciteSchema schema, List<String> path) {
      this.sql = sql;
      this.schema = schema;
      this.path = path;
    }

    @Override public boolean equals(Object obj) {
      return obj == this
          || obj instanceof QueryKey
          && sql.equals(((QueryKey) obj).sql)
          && schema.equals(((QueryKey) obj).schema)
          && path.equals(((QueryKey) obj).path);
    }

    @Override public int hashCode() {
      return com.google.common.base.Objects.hashCode(sql, schema, path);
    }
  }
}

// End MaterializationActor.java
