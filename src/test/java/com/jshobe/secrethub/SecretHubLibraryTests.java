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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.ptr.PointerByReference;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("SecretHub Library")
@Tag("integration")
class SecretHubLibraryTests {

  @Test
  @DisplayName("should create new client")
  void shouldCreateNewClient() {
    PointerByReference errMessage = new PointerByReference();
    SecretHubLibrary.Client client = SecretHubLibrary.INSTANCE.new_Client(errMessage);
    String error = Client.getErrorMessage(errMessage);
    assertNull(error);
    assertNotNull(client);
    SecretHubLibrary.INSTANCE.delete_Client(client);
  }

  @Nested
  @DisplayName("Given a client")
  class GivenAClient {

    private SecretHubLibrary.Client client;

    @BeforeEach
    void setUp() {
      PointerByReference errMessage = new PointerByReference();
      client = SecretHubLibrary.INSTANCE.new_Client(errMessage);
      String message = Client.getErrorMessage(errMessage);

      if (message != null) {
        client = null;
        throw new RuntimeException("Failed to create client: " + message);
      }
    }

    @AfterEach
    void tearDown() {
      if (client != null) {
        SecretHubLibrary.INSTANCE.delete_Client(client);
      }
    }

    @Test
    @DisplayName("should read secret")
    void shouldReadSecret() {
      PointerByReference errMessage = new PointerByReference();
      SecretHubLibrary.SecretVersion.ByValue version = SecretHubLibrary.INSTANCE
          .Client_Read(client, "jasonshobe/secrethub-java/test", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);
      assertNotNull(version);
      assertEquals("e86d9e8f-aa9d-40bd-a52c-515bbcfb6b40", version.SecretVersionID);
      assertEquals(2, version.Version);
      assertEquals(1612407296L, version.CreatedAt);
      assertEquals("ok", version.Status);
      assertNotNull(version.Secret);
      assertEquals("89e453e1-4962-48cf-afc0-4f169c49da6b", version.Secret.SecretID);
      assertEquals("1bb04779-89c7-4bf9-b7e9-7bdbec29841d", version.Secret.DirID);
      assertEquals("11843657-61ed-49e1-996e-b124bc4ec28e", version.Secret.RepoID);
      assertEquals("test", version.Secret.Name);
      assertEquals("xwzC2LKr6lwNHW2odRkCcgKuevUC_VW8NeNizsSEG6g=", version.Secret.BlindName);
      assertEquals(2, version.Secret.VersionCount);
      assertEquals(2, version.Secret.LatestVersion);
      assertEquals("ok", version.Secret.Status);
      assertEquals(1612384987L, version.Secret.CreatedAt);
      assertEquals("SUCCESS", version.Data);
    }

    @Test
    @DisplayName("should return error for missing secret")
    void shouldReturnErrorForMissingSecret() {
      PointerByReference errMessage = new PointerByReference();
      SecretHubLibrary.INSTANCE
          .Client_Read(client, "jasonshobe/secrethub-java/missing", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertEquals(
          "cannot find secret: \"jasonshobe/secrethub-java/missing\": Secret not found (server.secret_not_found) ",
          message);
    }

    @Test
    @DisplayName("should return error for closed client")
    void shouldReturnErrorForClosedClient() {
      SecretHubLibrary.INSTANCE.delete_Client(client);
      PointerByReference errMessage = new PointerByReference();
      SecretHubLibrary.INSTANCE
          .Client_Read(client, "jasonshobe/secrethub-java/missing", errMessage);
      client = null;
      String message = Client.getErrorMessage(errMessage);
      assertEquals("invalid client object", message);
    }

    @Test
    @DisplayName("should read secret string")
    void shouldReadSecretString() {
      PointerByReference errMessage = new PointerByReference();
      String secret = SecretHubLibrary.INSTANCE
          .Client_ReadString(client, "jasonshobe/secrethub-java/test", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);
      assertEquals("SUCCESS", secret);
    }

    @Test
    @DisplayName("should return error for missing secret string")
    void shouldReturnErrorForMissingSecretString() {
      PointerByReference errMessage = new PointerByReference();
      SecretHubLibrary.INSTANCE
          .Client_ReadString(client, "jasonshobe/secrethub-java/missing", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertEquals(
          "cannot find secret: \"jasonshobe/secrethub-java/missing\": Secret not found (server.secret_not_found) ",
          message);
    }

    @Test
    @DisplayName("should resolve secret")
    void shouldResolveSecret() {
      PointerByReference errMessage = new PointerByReference();
      String secret = SecretHubLibrary.INSTANCE
          .Client_Resolve(client, "secrethub://jasonshobe/secrethub-java/test", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);
      assertEquals("SUCCESS", secret);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("should resolve environment")
    void shouldResolveEnvironment() throws Exception {
      PointerByReference errMessage = new PointerByReference();
      String json = SecretHubLibrary.INSTANCE.Client_ResolveEnv(client, errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);
      assertNotNull(json);
      Map<String, String> env = new ObjectMapper().readValue(json, Map.class);
      assertTrue(env.containsKey("TEST_KEY"));
      assertEquals("SUCCESS", env.get("TEST_KEY"));
    }

    @Test
    @DisplayName("should return true for existing secret")
    void shouldReturnTrueForExistingSecret() {
      PointerByReference errMessage = new PointerByReference();
      boolean exists = SecretHubLibrary.INSTANCE
          .Client_Exists(client, "jasonshobe/secrethub-java/test", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);
      assertTrue(exists);
    }

    @Test
    @DisplayName("should return false for missing secret")
    void shouldReturnFalseForMissingSecret() {
      PointerByReference errMessage = new PointerByReference();
      boolean exists = SecretHubLibrary.INSTANCE
          .Client_Exists(client, "jasonshobe/secrethub-java/missing", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);
      assertFalse(exists);
    }

    @Test
    @DisplayName("should remove secret")
    void shouldRemoveSecret() {
      if (!secretExists("jasonshobe/secrethub-java/test-remove")) {
        writeSecret("jasonshobe/secrethub-java/test-remove", "SUCCESS");
      }

      boolean exists = secretExists("jasonshobe/secrethub-java/test-remove");
      assertTrue(exists);

      PointerByReference errMessage = new PointerByReference();
      SecretHubLibrary.INSTANCE
          .Client_Remove(client, "jasonshobe/secrethub-java/test-remove", errMessage);
      String message = Client.getErrorMessage(errMessage);
      assertNull(message);

      exists = secretExists("jasonshobe/secrethub-java/test-remove");
      assertFalse(exists);
    }

    @Test
    @DisplayName("should write secret")
    void shouldWriteSecret() {
      String expected = "TEST:" + System.currentTimeMillis();
      writeSecret("jasonshobe/secrethub-java/test-write", expected);
      String actual = readSecret("jasonshobe/secrethub-java/test-write");
      assertEquals(expected, actual);
    }

    private String readSecret(String path) {
      PointerByReference errMessage = new PointerByReference();
      String secret = SecretHubLibrary.INSTANCE.Client_ReadString(client, path, errMessage);
      String error = Client.getErrorMessage(errMessage);

      if (error != null) {
        throw new RuntimeException(error);
      }

      return secret;
    }

    private boolean secretExists(String path) {
      PointerByReference errMessage = new PointerByReference();
      boolean exists = SecretHubLibrary.INSTANCE.Client_Exists(client, path, errMessage);
      String error = Client.getErrorMessage(errMessage);

      if (error != null) {
        throw new RuntimeException(error);
      }

      return exists;
    }

    private void writeSecret(String path, String secret) {
      PointerByReference errMessage = new PointerByReference();
      SecretHubLibrary.INSTANCE.Client_Write(client, path, secret, errMessage);
      String error = Client.getErrorMessage(errMessage);

      if (error != null) {
        throw new RuntimeException(error);
      }
    }
  }
}
