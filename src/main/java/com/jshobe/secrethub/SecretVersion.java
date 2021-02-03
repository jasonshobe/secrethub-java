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
 * {@code SecretVersion} represents a version of a secret without any encrypted data.
 */
@Data
@NoArgsConstructor
public class SecretVersion {

  /**
   * The identifier of the version.
   */
  private UUID secretVersionId;

  /**
   * The secret.
   */
  private Secret secret;

  /**
   * The version number.
   */
  private int version;

  /**
   * The secret data.
   */
  private String data;

  /**
   * The date and time at which the version was created.
   */
  private LocalDateTime createdAt;

  /**
   * The status of the version.
   */
  private String status;

  /**
   * Creates a new instance of {@code SecretVersion}.
   *
   * @param version the version to copy.
   */
  SecretVersion(SecretHubLibrary.SecretVersion version) {
    setSecretVersionId(getUUID(version.SecretVersionID));
    setSecret(new Secret(version.Secret));
    setVersion(version.Version);
    setData(version.Data);
    setCreatedAt(getDateTime(version.CreatedAt));
    setStatus(version.Status);
  }
}
