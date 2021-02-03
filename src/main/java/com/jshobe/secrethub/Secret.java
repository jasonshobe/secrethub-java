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

import static com.jshobe.secrethub.Client.getDateTime;
import static com.jshobe.secrethub.Client.getUUID;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code Secret} represents a decrypted secret in SecretHub.
 */
@Data
@NoArgsConstructor
public class Secret {

  /**
   * The identifier of the secret.
   */
  private UUID secretId;

  /**
   * The identifier of the directory containing the secret.
   */
  private UUID directoryId;

  /**
   * The identifier of the repository containing the secret.
   */
  private UUID repositoryId;

  /**
   * The name of the secret.
   */
  private String name;

  /**
   * The blind name of the secret.
   */
  private String blindName;

  /**
   * The number of versions of the secret there are.
   */
  private int versionCount;

  /**
   * The latest version number of the secret.
   */
  private int latestVersion;

  /**
   * The status of the secret.
   */
  private String status;

  /**
   * The date and time at which the secret was created.
   */
  private LocalDateTime createdAt;

  /**
   * Creates a new instance of {@code Secret}.
   *
   * @param secret the secret to copy.
   */
  Secret(SecretHubLibrary.Secret secret) {
    setSecretId(getUUID(secret.SecretID));
    setDirectoryId(getUUID(secret.DirID));
    setRepositoryId(getUUID(secret.RepoID));
    setName(secret.Name);
    setBlindName(secret.BlindName);
    setVersionCount(secret.VersionCount);
    setLatestVersion(secret.LatestVersion);
    setStatus(secret.Status);
    setCreatedAt(getDateTime(secret.CreatedAt));
  }
}
