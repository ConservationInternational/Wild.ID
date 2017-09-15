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
package org.wildid.entity;

import java.util.Comparator;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class CameraTrapArrayComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof CameraTrapArray && o2 instanceof CameraTrapArray) {
            CameraTrapArray c1 = (CameraTrapArray) o1;
            CameraTrapArray c2 = (CameraTrapArray) o2;

            String[] names1;
            if (c1.getName().indexOf("-") != -1) {
                names1 = c1.getName().split("-");
            } else if (c1.getName().indexOf("_") != -1) {
                names1 = c1.getName().split("_");
            } else {
                names1 = new String[]{c1.getName()};
            }

            String[] names2;
            if (c2.getName().indexOf("-") != -1) {
                names2 = c2.getName().split("-");
            } else if (c2.getName().indexOf("_") != -1) {
                names2 = c2.getName().split("_");
            } else {
                names2 = new String[]{c2.getName()};
            }

            if (names1.length == names2.length) {
                int result = 0;
                for (int i = 0; i < names1.length; i++) {
                    try {
                        int number1 = Integer.parseInt(names1[i]);
                        int number2 = Integer.parseInt(names2[i]);
                        if (number1 < number2) {
                            result = -1;
                        } else if (number1 > number2) {
                            result = 1;
                        }
                    } catch (Exception ex) {
                        result = names1[i].compareTo(names2[i]);
                    }
                    if (result != 0) {
                        break;
                    }
                }

                return result;
            } else {
                return c1.getName().compareTo(c2.getName());
            }
        }

        return 0;
    }

}
