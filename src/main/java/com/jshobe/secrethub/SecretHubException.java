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

/**
 * {@code SecretHubException} signals that an error occurred during a call to the SecretHub client
 * library.
 */
public class SecretHubException extends Exception {

  /**
   * Creates a new instance of {@code SecretHubException}.
   *
   * @param message the error message.
   */
  public SecretHubException(String message) {
    super(message);
  }

  /**
   * Creates a new instance of {@code SecretHubException}.
   *
   * @param message the error message.
   * @param cause   the cause.
   */
  public SecretHubException(String message, Throwable cause) {
    super(message, cause);
  }
}
