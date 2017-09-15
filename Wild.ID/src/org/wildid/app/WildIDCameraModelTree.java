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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraModelComparator;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDCameraModelTree extends TreeView implements LanguageChangable {

    private LanguageModel language;
    private ObservableList<CameraModel> cameraModels;
    private CameraModelListTreeItem rootItem;

    public WildIDCameraModelTree(
            LanguageModel language,
            ObservableList<CameraModel> cameraModels,
            CameraModelListTreeItem rootItem) {

        super(rootItem);
        this.rootItem = rootItem;
        this.rootItem.setExpanded(true);

        this.language = language;
        this.cameraModels = cameraModels;

        for (String maker : getMakers()) {
            CameraModelMakerTreeItem makerItem = new CameraModelMakerTreeItem(language, maker);
            rootItem.getChildren().add(makerItem);

            for (CameraModel model : getModels(maker)) {
                CameraModelMakerModelTreeItem modelItem = new CameraModelMakerModelTreeItem(language, model);
                makerItem.getChildren().add(modelItem);
            }

        }

        this.setStyle("-fx-background-color:gray;");

        this.requestFocus();
        this.getSelectionModel().select(rootItem);

    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        setItemLanguage(this.rootItem, language);
    }

    private void setItemLanguage(TreeItem treeItem, LanguageModel language) {

        if (treeItem instanceof LanguageChangable) {
            ((LanguageChangable) treeItem).setLanguage(language);
        }

        treeItem.getChildren().stream().forEach((object) -> {
            setItemLanguage((TreeItem) object, language);
        });

    }

    public void setWildIDController(WildIDController controller) {
        this.getSelectionModel().selectedItemProperty().addListener(controller);
    }

    private List<String> getMakers() {
        List<String> makers = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>();
        this.cameraModels.stream().forEach((cm) -> {
            set.add(cm.getMaker());
        });
        makers.addAll(set);
        return makers;
    }

    private Set<CameraModel> getModels(String maker) {
        TreeSet<CameraModel> set = new TreeSet<>(new CameraModelComparator());
        this.cameraModels.stream().filter((cm) -> (cm.getMaker().equals(maker))).forEach((cm) -> {
            set.add(cm);
        });
        return set;
    }

    public void createCameraModel(CameraModel cameraModel) {

        CameraModelMakerModelTreeItem newTreeItem = new CameraModelMakerModelTreeItem(language, cameraModel);

        for (Object object : this.rootItem.getChildren()) {
            CameraModelMakerTreeItem makerTreeItem = (CameraModelMakerTreeItem) object;
            String maker = makerTreeItem.getMaker();
            if (maker.equals(cameraModel.getMaker())) {
                int i = 0;
                boolean added = false;
                for (Object obj : makerTreeItem.getChildren()) {
                    CameraModelMakerModelTreeItem modelTreeItem = (CameraModelMakerModelTreeItem) obj;
                    CameraModel model = modelTreeItem.getCameraModel();
                    if (cameraModel.getName().compareTo(model.getName()) == -1) {
                        makerTreeItem.getChildren().add(i, newTreeItem);
                        added = true;
                        break;
                    }
                    i++;
                }
                if (!added) {
                    makerTreeItem.getChildren().add(newTreeItem);
                }
            }
        }
    }

    public void updateCameraModel(String oldMaker, String oldModel, CameraModel cameraModel) {

        CameraModelMakerModelTreeItem oldTreeItem = getMakerModelTreeItem(oldMaker, oldModel);
        if (oldTreeItem != null) {
            TreeItem treeItem = oldTreeItem.getParent();
            treeItem.getChildren().remove(oldTreeItem);
        }
        oldTreeItem.setCameraModel(cameraModel);
        addMakerModelTreeItem(oldTreeItem);
    }

    public void addMakerModelTreeItem(CameraModelMakerModelTreeItem treeItem) {

        CameraModel cameraModel = treeItem.getCameraModel();

        for (Object object : this.rootItem.getChildren()) {
            CameraModelMakerTreeItem makerTreeItem = (CameraModelMakerTreeItem) object;
            String maker = makerTreeItem.getMaker();
            if (maker.equals(cameraModel.getMaker())) {
                int i = 0;
                boolean added = false;
                for (Object obj : makerTreeItem.getChildren()) {
                    CameraModelMakerModelTreeItem modelTreeItem = (CameraModelMakerModelTreeItem) obj;
                    CameraModel model = modelTreeItem.getCameraModel();
                    if (cameraModel.getName().compareTo(model.getName()) == -1) {
                        makerTreeItem.getChildren().add(i, treeItem);
                        added = true;
                        break;
                    }
                    i++;
                }
                if (!added) {
                    makerTreeItem.getChildren().add(treeItem);
                    break;
                }
            }
        }
    }

    public CameraModelMakerModelTreeItem getMakerModelTreeItem(String maker, String model) {

        for (Object object : this.rootItem.getChildren()) {
            CameraModelMakerTreeItem makerTreeItem = (CameraModelMakerTreeItem) object;
            String aMaker = makerTreeItem.getMaker();
            if (maker.equals(aMaker)) {
                for (Object obj : makerTreeItem.getChildren()) {
                    CameraModelMakerModelTreeItem modelTreeItem = (CameraModelMakerModelTreeItem) obj;
                    if (modelTreeItem.getValue().equals(model)) {
                        return modelTreeItem;
                    }
                }
            }
        }
        return null;
    }

    public void deleteCameraModel(CameraModel cameraModel) {

        CameraModelMakerModelTreeItem treeItem = getMakerModelTreeItem(cameraModel.getMaker(), cameraModel.getName());
        if (treeItem != null) {
            treeItem.getParent().getChildren().remove(treeItem);
        }

    }

}
