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
import com.sun.jna.Platform;

public class CLibrary {

  public static void putenv(String key, String value) {
    String envstring = key + "=" + value;

    if (Platform.isWindows()) {
      WindowsCLibrary.INSTANCE._putenv(envstring);
    } else {
      LinuxCLibrary.INSTANCE.putenv(envstring);
    }
  }

  public interface LinuxCLibrary extends Library {

    int putenv(String envstring);

    LinuxCLibrary INSTANCE = Native.load("c", LinuxCLibrary.class);
  }

  public interface WindowsCLibrary extends Library {

    int _putenv(String envstring);

    WindowsCLibrary INSTANCE = Native.load("msvcrt", WindowsCLibrary.class);
  }
}
