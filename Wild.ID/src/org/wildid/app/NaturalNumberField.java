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

import javafx.scene.control.TextField;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class NaturalNumberField extends TextField {

    private int max = 1440;
    private int min = 1;

    public NaturalNumberField(int max, int min) {
        this.max = max;
        this.min = min;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        boolean ok = true;
        try {
            //String tmp = this.getText() + text;
            String tmp = this.getText();
            String left = tmp.substring(0, start);
            String right = tmp.substring(end);
            tmp = left + text + right;

            if (tmp.equals("")) {
                ok = true;
            } else {
                int i = Integer.parseInt(tmp);
                ok = i <= max && i >= min;
            }
        } catch (Exception ex) {
            ok = false;
        }
        if (ok) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        boolean ok = true;
        try {
            String tmp = text;
            if (tmp.equals("-") || tmp.equals("")) {
                ok = true;
            } else {
                int i = Integer.parseInt(tmp);
                ok = i <= max && i >= min;
            }
        } catch (Exception ex) {
            ok = false;
        }
        if (ok) {
            super.replaceSelection(text);
        }
    }

}
