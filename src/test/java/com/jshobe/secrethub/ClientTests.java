/*
 * Copyright 2021 Jason Shobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jshobe.secrethub;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client")
class ClientTests {

  @Mock
  SecretHubLibrary library;

  @Test
  @DisplayName("should throw exception when new client fails")
  void shouldThrowExceptionWhenNewClientFails() {
    Memory error = createErrorMessage("test error");
    when(library.new_Client(any(PointerByReference.class))).thenAnswer(
        (Answer<SecretHubLibrary.Client>) invocation -> {
          PointerByReference pointer = invocation.getArgument(0);
          setErrorMessage(pointer, error);
          return new SecretHubLibrary.Client();
        });

    SecretHubException thrown = assertThrows(SecretHubException.class, () -> new Client(library));
    assertEquals("test error", thrown.getMessage());
    verify(library).new_Client(any(PointerByReference.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should return secret")
  void shouldReturnSecret() throws Exception {
    SecretHubLibrary.SecretVersion.ByValue version = new SecretHubLibrary.SecretVersion.ByValue();
    version.SecretVersionID = "0dd95e7b-a5c3-4982-b80c-6bd9e4e33c56";
    version.Version = 1;
    version.CreatedAt = 1612384987L;
    version.Status = "ok";
    version.Data = "SUCCESS";
    version.Secret = new SecretHubLibrary.Secret();
    version.Secret.SecretID = "89e453e1-4962-48cf-afc0-4f169c49da6b";
    version.Secret.DirID = "1bb04779-89c7-4bf9-b7e9-7bdbec29841d";
    version.Secret.RepoID = "11843657-61ed-49e1-996e-b124bc4ec28e";
    version.Secret.Name = "test";
    version.Secret.BlindName = "xwzC2LKr6lwNHW2odRkCcgKuevUC_VW8NeNizsSEG6g=";
    version.Secret.VersionCount = 1;
    version.Secret.LatestVersion = 1;
    version.Secret.Status = "ok";
    version.Secret.CreatedAt = 1612384987L;

    when(library
        .Client_Read(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class))).thenReturn(version);

    SecretVersion expected = new SecretVersion();
    expected.setSecretVersionId(Client.getUUID("0dd95e7b-a5c3-4982-b80c-6bd9e4e33c56"));
    expected.setVersion(1);
    expected.setCreatedAt(Client.getDateTime(1612384987L));
    expected.setStatus("ok");
    expected.setData("SUCCESS");
    expected.setSecret(new Secret());
    expected.getSecret().setSecretId(Client.getUUID("89e453e1-4962-48cf-afc0-4f169c49da6b"));
    expected.getSecret().setDirectoryId(Client.getUUID("1bb04779-89c7-4bf9-b7e9-7bdbec29841d"));
    expected.getSecret().setRepositoryId(Client.getUUID("11843657-61ed-49e1-996e-b124bc4ec28e"));
    expected.getSecret().setName("test");
    expected.getSecret().setBlindName("xwzC2LKr6lwNHW2odRkCcgKuevUC_VW8NeNizsSEG6g=");
    expected.getSecret().setVersionCount(1);
    expected.getSecret().setLatestVersion(1);
    expected.getSecret().setStatus("ok");
    expected.getSecret().setCreatedAt(Client.getDateTime(1612384987L));

    try (Client client = createClient()) {
      SecretVersion actual = client.read("jasonshobe/secrethub-java/test");
      assertEquals(expected, actual);
    }

    verify(library)
        .Client_Read(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should throw exception when read error is set")
  void shouldThrowExceptionWhenReadErrorIsSet() throws Exception {
    Memory error = createErrorMessage("missing secret");
    when(library
        .Client_Read(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class)))
        .thenAnswer((Answer<SecretHubLibrary.SecretVersion.ByValue>) invocation -> {
          PointerByReference pointer = invocation.getArgument(2);
          setErrorMessage(pointer, error);
          return new SecretHubLibrary.SecretVersion.ByValue();
        });

    try (Client client = createClient()) {
      SecretHubException thrown = assertThrows(SecretHubException.class,
          () -> client.read("jasonshobe/secrethub-java/test"));
      assertEquals("missing secret", thrown.getMessage());
    }

    verify(library)
        .Client_Read(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should throw exception when client is closed")
  void shouldThrowExceptionWhenClientIsClosed() throws Exception {
    Client client = createClient();
    client.close();
    IllegalStateException thrown = assertThrows(IllegalStateException.class,
        () -> client.read("jasonshobe/secrethub-java"));
    assertEquals("The client has been closed", thrown.getMessage());
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should return secret string")
  void shouldReturnSecretString() throws Exception {
    when(library
        .Client_ReadString(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class))).thenReturn("SUCCESS");

    try (Client client = createClient()) {
      String actual = client.readString("jasonshobe/secrethub-java/test");
      assertEquals("SUCCESS", actual);
    }

    verify(library)
        .Client_ReadString(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should resolve")
  void shouldResolve() throws Exception {
    when(library
        .Client_Resolve(any(SecretHubLibrary.Client.class),
            eq("secrethub://jasonshobe/secrethub-java/test"),
            any(PointerByReference.class))).thenReturn("SUCCESS");

    try (Client client = createClient()) {
      String actual = client.resolve("secrethub://jasonshobe/secrethub-java/test");
      assertEquals("SUCCESS", actual);
    }

    verify(library)
        .Client_Resolve(any(SecretHubLibrary.Client.class),
            eq("secrethub://jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should resolve env")
  void shouldResolveEnv() throws Exception {
    String json = "{\"TEST_KEY\":\"SUCCESS\"}";
    when(library
        .Client_ResolveEnv(any(SecretHubLibrary.Client.class), any(PointerByReference.class)))
        .thenReturn(json);

    Map<String, String> expected = new HashMap<>();
    expected.put("TEST_KEY", "SUCCESS");

    try (Client client = createClient()) {
      Map<String, String> actual = client.resolveEnv();
      assertEquals(expected, actual);
    }

    verify(library)
        .Client_ResolveEnv(any(SecretHubLibrary.Client.class), any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should throw exception for invalid JSON")
  void shouldThrowExceptionForInvalidJson() throws Exception {
    String json = "{\"TEST_KEY\":}";
    when(library
        .Client_ResolveEnv(any(SecretHubLibrary.Client.class), any(PointerByReference.class)))
        .thenReturn(json);

    try (Client client = createClient()) {
      SecretHubException thrown = assertThrows(SecretHubException.class, client::resolveEnv);
      assertEquals("Failed to parse environment JSON", thrown.getMessage());
      assertThat(thrown.getCause(), instanceOf(JsonParseException.class));
    }

    verify(library)
        .Client_ResolveEnv(any(SecretHubLibrary.Client.class), any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should return true for existing")
  void shouldReturnTrueForExisting() throws Exception {
    when(library
        .Client_Exists(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class))).thenReturn(true);

    try (Client client = createClient()) {
      boolean actual = client.exists("jasonshobe/secrethub-java/test");
      assertTrue(actual);
    }

    verify(library)
        .Client_Exists(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should return false for missing")
  void shouldReturnFalseForMissing() throws Exception {
    when(library
        .Client_Exists(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class))).thenReturn(false);

    try (Client client = createClient()) {
      boolean actual = client.exists("jasonshobe/secrethub-java/test");
      assertFalse(actual);
    }

    verify(library)
        .Client_Exists(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should remove secret")
  void shouldRemoveSecret() throws Exception {
    try (Client client = createClient()) {
      client.remove("jasonshobe/secrethub-java/test");
    }

    verify(library)
        .Client_Remove(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should write secret")
  void shouldWriteSecret() throws Exception {
    try (Client client = createClient()) {
      client.write("jasonshobe/secrethub-java/test", "SUCCESS");
    }

    verify(library)
        .Client_Write(any(SecretHubLibrary.Client.class), eq("jasonshobe/secrethub-java/test"),
            eq("SUCCESS"), any(PointerByReference.class));
    verify(library).delete_Client(any(SecretHubLibrary.Client.class));
    verifyNoMoreInteractions(library);
  }

  @Test
  @DisplayName("should return UUID")
  void shouldReturnUUID() {
    UUID actual = Client.getUUID("0dd95e7b-a5c3-4982-b80c-6bd9e4e33c56");
    assertEquals("0dd95e7b-a5c3-4982-b80c-6bd9e4e33c56", actual.toString());
  }

  @Test
  @DisplayName("should return null for null UUID")
  void shouldReturnNullForNullUUID() {
    UUID actual = Client.getUUID(null);
    assertNull(actual);
  }

  @Test
  @DisplayName("should return date time")
  void shouldReturnDateTime() {
    LocalDateTime expected = LocalDateTime
        .ofEpochSecond(1612373400L, 0, OffsetDateTime.now().getOffset());
    LocalDateTime actual = Client.getDateTime(1612373400L);
    assertEquals(expected, actual);
  }

  private Client createClient() throws SecretHubException {
    SecretHubLibrary.Client struct = new SecretHubLibrary.Client();
    struct.ID = 1L;
    when(library.new_Client(any(PointerByReference.class))).thenReturn(struct);
    Client client = new Client(library);
    verify(library).new_Client(any(PointerByReference.class));
    return client;
  }

  private Memory createErrorMessage(String message) {
    byte[] data = message.getBytes(StandardCharsets.US_ASCII);
    Memory memory = new Memory(data.length + 1);
    memory.write(0L, data, 0, data.length);
    memory.setByte(data.length, (byte) 0);
    return memory;
  }

  private void setErrorMessage(PointerByReference pointer, Memory message) {
    pointer.getPointer().setLong(0L, Pointer.nativeValue(message));
  }
}
