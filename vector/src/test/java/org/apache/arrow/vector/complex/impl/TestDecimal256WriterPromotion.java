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
package org.apache.arrow.vector.complex.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.complex.ListVector;
import org.junit.jupiter.api.Test;

public class TestDecimal256WriterPromotion {
  @Test
  public void testDecimal256InPromotedUnion() {
    BigDecimal narrow = new BigDecimal("123.45");
    BigDecimal wide = new BigDecimal("123456789012345678901234567890123456789.01");
    try (BufferAllocator allocator = new RootAllocator();
        ListVector vector = ListVector.empty("decimals", allocator)) {
      UnionListWriter writer = vector.getWriter();
      writer.startList();
      writer.decimal().writeDecimal(narrow);
      writer.decimal256().writeDecimal256(wide);
      writer.decimal().writeDecimal(narrow.negate());
      writer.decimal256().writeDecimal256(wide.negate());
      writer.endList();
      vector.setValueCount(1);
      assertEquals(
          Arrays.asList(narrow, wide, narrow.negate(), wide.negate()), vector.getObject(0));
      vector.validateFull();
    }
  }
}
