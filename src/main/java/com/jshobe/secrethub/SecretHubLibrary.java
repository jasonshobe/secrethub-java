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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.PointerByReference;

/**
 * {@code SecretHubLibrary} is an interface that provides the JNA mappings to the the
 * <a href="https://github.com/secrethub/secrethub-go">secrethub-go</a> native library.
 *
 * <p>This interface should not be used directly. Instead, the {@link Client} class should be
 * used.</p>
 */
public interface SecretHubLibrary extends Library {

  /**
   * Creates a new Go client.
   *
   * @param errMessage a pointer where any error message will be stored.
   * @return a new client structure.
   */
  Client new_Client(PointerByReference errMessage);

  /**
   * Frees the memory occupied by the given client and clears its corresponding entry in the map.
   *
   * @param client the client to close.
   */
  void delete_Client(Client client);

  /**
   * Retrieves a secret by its path.
   *
   * @param client     the client.
   * @param path       the path to the secret.
   * @param errMessage a pointer where any error message will be stored.
   * @return the secret.
   */
  SecretVersion.ByValue Client_Read(Client client, String path, PointerByReference errMessage);

  /**
   * Retrieves a secret as a string.
   *
   * @param client     the client.
   * @param path       the path to the secret.
   * @param errMessage a pointer where any error message will be stored.
   * @return the secret.
   */
  String Client_ReadString(Client client, String path, PointerByReference errMessage);

  /**
   * Fetches the value of a secret from SecretHub, when the <i>ref</i> parameter has the format
   * {@code secrethub://<path>}. Otherwise it returns <i>ref</i> unchanged, as an array of bytes.
   *
   * @param client     the client.
   * @param ref        the secret reference.
   * @param errMessage a pointer where any error message will be stored.
   * @return the secret.
   */
  String Client_Resolve(Client client, String ref, PointerByReference errMessage);

  /**
   * Takes a map of environment variables and replaces the values of those which store references
   * of secrets in SecretHub ({@code secrethub://path})
   *
   * @param client     the client.
   * @param errMessage a pointer where any error message will be stored.
   * @return the JSON-encoded environment variables with the secrets replaced.
   */
  String Client_ResolveEnv(Client client, PointerByReference errMessage);

  /**
   * Checks if a secret exists at <i>path</i>.
   *
   * @param client     the client.
   * @param path       the path to the secret.
   * @param errMessage a pointer where any error message will be stored.
   * @return {@code true} if the secret exists or {@code false} if not.
   */
  boolean Client_Exists(Client client, String path, PointerByReference errMessage);

  /**
   * Deletes the secret found at <i>path</i>, if it exists.
   *
   * @param client     the client.
   * @param path       the path to the secret.
   * @param errMessage a pointer where any error message will be stored.
   */
  void Client_Remove(Client client, String path, PointerByReference errMessage);

  /**
   * Writes a secret containing the contents of <i>secret</i> at <i>path</i>.
   *
   * @param client     the client.
   * @param path       the path to the secret.
   * @param secret     the value of the secret.
   * @param errMessage a pointer where any error message will be stored.
   */
  void Client_Write(Client client, String path, String secret, PointerByReference errMessage);

  /**
   * The shared instance of the library.
   */
  SecretHubLibrary INSTANCE = Native.load("secrethub", SecretHubLibrary.class);

  /**
   * The SecretHub {@code Secret} structure.
   */
  @FieldOrder({
      "SecretID", "DirID", "RepoID", "Name", "BlindName", "VersionCount", "LatestVersion",
      "Status", "CreatedAt"})
  class Secret extends Structure {

    public String SecretID;
    public String DirID;
    public String RepoID;
    public String Name;
    public String BlindName;
    public int VersionCount;
    public int LatestVersion;
    public String Status;
    public long CreatedAt;

    public Secret() {
    }

    public Secret(Pointer p) {
      super(p);
    }
  }

  /**
   * The SecretHub {@code SecretVersion} structure.
   */
  @FieldOrder({"SecretVersionID", "Secret", "Version", "Data", "CreatedAt", "Status"})
  class SecretVersion extends Structure {

    public String SecretVersionID;
    public Secret Secret;
    public int Version;
    public String Data;
    public long CreatedAt;
    public String Status;

    public SecretVersion() {
    }

    public SecretVersion(Pointer p) {
      super(p);
    }

    public static class ByValue extends SecretVersion implements Structure.ByValue {
      public ByValue() {
      }

      public ByValue(Pointer p) {
        super(p);
      }
    }
  }

  /**
   * The SecretHub {@code Client} structure.
   */
  @FieldOrder({"ID"})
  class Client extends Structure {

    public long ID;

    public Client() {
    }

    public Client(Pointer p) {
      super(p);
    }
  }
}
