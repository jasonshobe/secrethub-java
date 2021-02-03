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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Client} provides methods to access secrets stored in SecretHub.
 */
public class Client implements AutoCloseable {

  private final SecretHubLibrary library;
  private volatile SecretHubLibrary.Client client;

  /**
   * Creates a new instance of {@code Client}.
   *
   * @throws SecretHubException if the client could not be created.
   */
  public Client() throws SecretHubException {
    this(SecretHubLibrary.INSTANCE);
  }

  /**
   * Creates a new instance of {@code Client}.
   *
   * @param library the SecretHub library instance.
   * @throws SecretHubException if the client could not be created.
   */
  Client(SecretHubLibrary library) throws SecretHubException {
    this.library = library;
    PointerByReference errMessage = new PointerByReference();
    this.client = library.new_Client(errMessage);
    throwException(errMessage);
  }

  /**
   * Retrieves a secret by its path.
   *
   * @param path the path to the secret.
   * @return the secret.
   * @throws SecretHubException    if an error prevented the secret from being obtained.
   * @throws IllegalStateException if this client has been closed.
   */
  public SecretVersion read(String path) throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    SecretHubLibrary.SecretVersion result = library.Client_Read(client, path, errMessage);
    throwException(errMessage);

    return new SecretVersion(result);
  }

  /**
   * Retrieves a secret as a string.
   *
   * @param path the path to the secret.
   * @return the secret.
   * @throws SecretHubException    if an error prevented the secret from being obtained.
   * @throws IllegalStateException if this client has been closed.
   */
  public String readString(String path) throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    String result = library.Client_ReadString(client, path, errMessage);
    throwException(errMessage);

    return result;
  }

  /**
   * Fetches the value of a secret from SecretHub, when the <i>reference</i> parameter has the
   * format {@code secrethub://<path>}. Otherwise it returns <i>reference</i> unchanged.
   *
   * @param reference the reference.
   * @return the secret.
   * @throws SecretHubException    if an error prevented the secret from being resolved.
   * @throws IllegalStateException if this client has been closed.
   */
  public String resolve(String reference) throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    String result = library.Client_Resolve(client, reference, errMessage);
    throwException(errMessage);

    return result;
  }

  /**
   * Replaces the values of any environment variables that store references to secrets in SecretHub
   * ({@code secrethub://path}).
   *
   * @return a map of resolved environment variables.
   * @throws SecretHubException    if an error prevented the environment from being resolved.
   * @throws IllegalStateException if this client has been closed.
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> resolveEnv() throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    String json = library.Client_ResolveEnv(client, errMessage);
    throwException(errMessage);

    Map<String, String> result;

    try {
      result = new ObjectMapper().readValue(json, Map.class);
    } catch (Exception e) {
      throw new SecretHubException("Failed to parse environment JSON", e);
    }

    return result;
  }

  /**
   * Checks if a secret exists at <i>path</i>.
   *
   * @param path the path to the secret.
   * @return {@code true} if the secret exists or {@code false} if not.
   * @throws SecretHubException    if an error prevented the existence from being determined.
   * @throws IllegalStateException if this client has been closed.
   */
  public boolean exists(String path) throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    boolean result = library.Client_Exists(client, path, errMessage);
    throwException(errMessage);

    return result;
  }

  /**
   * Deletes the secret found at <i>path</i>, if it exists.
   *
   * @param path the path to the secret.
   * @throws SecretHubException    if an error prevented the secret from being removed.
   * @throws IllegalStateException if this client has been closed.
   */
  public void remove(String path) throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    library.Client_Remove(client, path, errMessage);
    throwException(errMessage);
  }

  /**
   * Writes a secret containing the contents of <i>secret</i> at <i>path</i>.
   *
   * @param path   the path to the secret.
   * @param secret the value of the secret.
   * @throws SecretHubException    if an error prevented the secret from being written.
   * @throws IllegalStateException if this client has been closed.
   */
  public void write(String path, String secret) throws SecretHubException {
    checkClient();

    PointerByReference errMessage = new PointerByReference();
    library.Client_Write(client, path, secret, errMessage);
    throwException(errMessage);
  }

  @Override
  public void close() {
    if (client != null) {
      library.delete_Client(client);
      client = null;
    }
  }

  /**
   * Creates a {@link UUID} from its string representation.
   *
   * @param uuid the UUID string representation.
   * @return the UUID or {@code null} if <i>uuid</i> is {@code null}.
   */
  static UUID getUUID(String uuid) {
    return uuid == null ? null : UUID.fromString(uuid);
  }

  /**
   * Gets the date and time for a timestamp.
   *
   * @param timestamp the timestamp.
   * @return the local date and time.
   */
  static LocalDateTime getDateTime(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
  }

  /**
   * Gets the error message from a pointer.
   *
   * @param errMessage the error message pointer.
   * @return the error message or {@code null} if it was not set.
   */
  static String getErrorMessage(PointerByReference errMessage) {
    Pointer pointer = errMessage.getPointer();
    long peer = pointer.getLong(0L);

    if (peer != 0L) {
      Pointer msgPointer = new Pointer(peer);
      String message = msgPointer.getString(0L);

      if (!message.isEmpty()) {
        return message;
      }
    }

    return null;
  }

  /**
   * Throws an exception if the error message has been set.
   *
   * @param errMessage a pointer to the error message string.
   * @throws SecretHubException if the error message was set.
   */
  private void throwException(PointerByReference errMessage) throws SecretHubException {
    String message = getErrorMessage(errMessage);

    if (message != null) {
      throw new SecretHubException(message);
    }
  }

  /**
   * Checks that the client has not been closed.
   *
   * @throws IllegalStateException if the client has been closed.
   */
  private void checkClient() throws IllegalStateException {
    if (client == null) {
      throw new IllegalStateException("The client has been closed");
    }
  }
}
