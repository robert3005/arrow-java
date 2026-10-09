/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.arrow.driver.jdbc.converter.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Decimal256Vector;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.calcite.avatica.ColumnMetaData;
import org.apache.calcite.avatica.remote.TypedValue;
import org.junit.jupiter.api.Test;

public class DecimalAvaticaParameterConverterTest {
  @Test
  public void testBindDecimal256() {
    ArrowType.Decimal type = new ArrowType.Decimal(76, 2, 256);
    DecimalAvaticaParameterConverter converter = new DecimalAvaticaParameterConverter(type);
    BigDecimal value = new BigDecimal("123456789012345678901234567890123456789.01");
    try (BufferAllocator allocator = new RootAllocator();
        Decimal256Vector vector = new Decimal256Vector("decimal", allocator, 76, 2)) {
      assertTrue(
          converter.bindParameter(vector, TypedValue.ofLocal(ColumnMetaData.Rep.NUMBER, value), 0));
      assertTrue(
          converter.bindParameter(
              vector, TypedValue.ofLocal(ColumnMetaData.Rep.NUMBER, value.negate()), 1));
      vector.setValueCount(2);
      assertEquals(value, vector.getObject(0));
      assertEquals(value.negate(), vector.getObject(1));
    }
  }
}
