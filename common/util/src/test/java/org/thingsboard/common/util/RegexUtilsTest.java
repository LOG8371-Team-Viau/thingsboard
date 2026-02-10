/**
 * Copyright © 2016-2026 The Thingsboard Authors
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
package org.thingsboard.common.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class RegexUtilsTest {

    @Test
    void matchesReturnsTrueForMatchingInput() {
        Pattern pattern = Pattern.compile("^[a-z]+$");
        assertThat(RegexUtils.matches("abc", pattern)).isTrue();
    }

    @Test
    void matchesReturnsFalseForNonMatchingInput() {
        Pattern pattern = Pattern.compile("^[a-z]+$");
        assertThat(RegexUtils.matches("123", pattern)).isFalse();
    }

    @Test
    void getMatchReturnsMatchingGroup() {
        Pattern pattern = Pattern.compile("(\\d+)");
        assertThat(RegexUtils.getMatch("abc123def", pattern, 1)).isEqualTo("123");
    }

    @Test
    void getMatchReturnsNullWhenNoMatch() {
        Pattern pattern = Pattern.compile("(\\d+)");
        assertThat(RegexUtils.getMatch("abcdef", pattern, 1)).isNull();
    }

}
