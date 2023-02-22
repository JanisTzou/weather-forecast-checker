/*
 * Copyright 2021 Janis Tzoumas
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

package com.google.weatherforecastchecker.htmlunit;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.logging.Level;

@NoArgsConstructor
@Log4j2
public class HtmlUnitClientFactory {

    public static WebClient startDriver() {
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setTimeout(15000);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
        webClient.getOptions().setJavaScriptEnabled(false);
        log.debug("Started new client: {}", webClient);
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        return webClient;
    }

}
