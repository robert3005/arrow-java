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
package org.apache.arrow.vector.ipc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestDecimalJsonBufferCleanup {
  @TempDir Path tempDir;

  @ParameterizedTest
  @ValueSource(ints = {128, 256})
  public void testMalformedDecimalReleasesBuffers(int bitWidth) throws Exception {
    Schema schema =
        new Schema(
            Collections.singletonList(
                Field.nullable("decimal", new ArrowType.Decimal(10, 2, bitWidth))));
    Path path = tempDir.resolve("malformed-decimal.json");
    String json =
        "{\"schema\":"
            + schema.toJson()
            + ",\"batches\":[{\"count\":2,\"columns\":[{\"name\":\"decimal\",\"count\":2,"
            + "\"VALIDITY\":[1,1],\"DATA\":[\"123\",\"invalid\"]}]}]}";
    Files.write(path, json.getBytes(StandardCharsets.UTF_8));
    try (BufferAllocator allocator = new RootAllocator()) {
      try (JsonFileReader reader = new JsonFileReader(path.toFile(), allocator);
          VectorSchemaRoot root = VectorSchemaRoot.create(reader.start(), allocator)) {
        assertThrows(NumberFormatException.class, () -> reader.read(root));
      }
      assertEquals(0, allocator.getAllocatedMemory());
    }
  }
}
