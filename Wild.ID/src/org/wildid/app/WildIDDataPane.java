/*
Copyright (c) 2007 The Regents of the University of California

Permission to use, copy, modify, and distribute this software and its documentation
for educational, research and non-profit purposes, without fee, and without a written
agreement is hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Permission to make commercial use of this software may be obtained
by contacting:
Technology Transfer Office
9500 Gilman Drive, Mail Code 0910
University of California
La Jolla, CA 92093-0910
(858) 534-5815
invent@ucsd.edu

THIS SOFTWARE IS PROVIDED BY THE REGENTS OF THE UNIVERSITY OF CALIFORNIA AND
CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.wildid.app;

import javafx.scene.layout.Pane;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public abstract class WildIDDataPane extends Pane implements LanguageChangable {

    final protected static String BG_COLOR_STYLE
            = " -fx-background-color: #f0f0f0;";

    final protected static String TEXT_STYLE
            = " -fx-font-weight:normal;"
            + " -fx-color: #f0f0f0;";//-fx-font-size:12px -fx-font-family: Verdana;

    final protected static String TITLE_STYLE
            = " -fx-font-weight:normal;"
            + " -fx-text-fill: #ffffff;"
            + " -fx-font-size:16px;"
            + " -fx-font-family: Verdana;";

    final protected static String SUBTITLE_STYLE
            = " -fx-font-weight:bold;"
            + " -fx-color: #f0f0f0;"
            + " -fx-font-size:14;";

    final protected static String REQUIRED_STYLE
            = " -fx-text-box-border: red;"
            + " -fx-focus-color: red;";

    final protected static String BG_TITLE_STYLE
            = " -fx-background-color: #2a68af;";

    final protected static String INDEX_COLUMN_STYLE
            = " -fx-text-fill: #333333;"
            + " -fx-background-color: #efefef;"
            + " -fx-alignment: CENTER-RIGHT;"
            + " -fx-padding: 0 10px 0 0;"
            + " -fx-border-color: #fefefe;"
            + " -fx-border-width: 1px;";

    final protected static String BTN_TRANSPARENT_STYLE
            = " -fx-background-color: transparent;";

    final protected static String ALERT_DANGER_DIV
            = " -fx-background-color: #f2dede;"
            + " -fx-border-color: #ebccd1;"
            + " -fx-padding: 20;"
            + " -fx-background-radius: 10;"
            + " -fx-border-radius: 10;";

    final protected static String ALERT_DANGER_TEXT
            = " -fx-fill: #a94442;"
            + " -fx-font-size: 12px;";

    final protected static String ALERT_WARNING_DIV
            = " -fx-background-color: #fcf8e3;"
            + " -fx-border-color: #faebcc;"
            + " -fx-padding: 20;"
            + " -fx-background-radius: 10;"
            + " -fx-border-radius: 10;";

    final protected static String ALERT_WARNING_TEXT
            = " -fx-fill: #8a6d3b;"
            + " -fx-font-size: 12px;";

    final protected static String ALERT_INFO_DIV
            = " -fx-background-color: #d9edf7;"
            + " -fx-border-color: #bce8f1;"
            + " -fx-padding: 20;"
            + " -fx-background-radius: 10;"
            + " -fx-border-radius: 10;";

    final protected static String ALERT_INFO_TEXT
            = " -fx-fill: #31708f;"
            + " -fx-font-size: 12px;";

    final protected static String ALERT_SUCCESS_DIV
            = " -fx-background-color: #dff0d8;"
            + " -fx-border-color: #d6e9c6;"
            + " -fx-padding: 20;"
            + " -fx-background-radius: 10;"
            + " -fx-border-radius: 10;";

    final protected static String ALERT_SUCCESS_TEXT
            = " -fx-fill: #3c763d;"
            + " -fx-font-size: 12px;";

    final protected static String GRAY_DIV
            = " -fx-background-color: #eeeeee;"
            + " -fx-border-color: #aaaaaa;"
            + " -fx-padding: 20;"
            + " -fx-background-radius: 10;"
            + " -fx-border-radius: 10;";

    abstract public void setWildIDController(WildIDController controller);
}
