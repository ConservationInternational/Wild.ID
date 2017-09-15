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

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MultiTouchImageView extends StackPane {

    private ImageView imageView;
    private double lastX, lastY, startScale, startRotate;

    public MultiTouchImageView(Image img) {
        setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.5), 8, 0, 0, 2));

        imageView = new ImageView(img);
        imageView.setSmooth(true);
        getChildren().add(imageView);

        // This value may need tuning:
        Duration maxTimeBetweenSequentialClicks = Duration.millis(300);

        PauseTransition clickTimer = new PauseTransition(maxTimeBetweenSequentialClicks);
        final IntegerProperty sequentialClickCount = new SimpleIntegerProperty(0);
        clickTimer.setOnFinished(event -> {
            int count = sequentialClickCount.get();
            if (count == 1) {
                setScaleX(getScaleX() * 1.2);
                setScaleY(getScaleY() * 1.2);
            } else if (count > 1) {
                setScaleX(getScaleX() / 1.2);
                setScaleY(getScaleY() / 1.2);
            }
            sequentialClickCount.set(0);
        });

        setOnMouseClicked((MouseEvent event) -> {
            sequentialClickCount.set(sequentialClickCount.get() + 1);
            clickTimer.playFromStart();
        });

        setOnMousePressed((MouseEvent event) -> {
            lastX = event.getX();
            lastY = event.getY();
            toFront();
        });

        setOnMouseDragged((MouseEvent event) -> {
            double xLoc = imageView.getBoundsInLocal().getMinX();
            double yLoc = imageView.getBoundsInLocal().getMinY();
            imageView.setTranslateX(xLoc + (event.getX() - lastX));
            imageView.setTranslateY(yLoc + (event.getY() - lastY));
            sequentialClickCount.set(-1);
        });

        addEventHandler(ZoomEvent.ZOOM_STARTED, (ZoomEvent event) -> {
            startScale = getScaleX();
        });

        addEventHandler(ZoomEvent.ZOOM, (ZoomEvent event) -> {
            setScaleX(startScale * event.getTotalZoomFactor());
            setScaleY(startScale * event.getTotalZoomFactor());
        });

        addEventHandler(RotateEvent.ROTATION_STARTED, (RotateEvent event) -> {
            startRotate = getRotate();
        });

        addEventHandler(RotateEvent.ROTATE, (RotateEvent event) -> {
            setRotate(startRotate + event.getTotalAngle());
        });
    }
}
