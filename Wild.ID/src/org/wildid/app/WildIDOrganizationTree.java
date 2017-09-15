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

import javafx.collections.ObservableList;
import javafx.scene.control.TreeView;
import org.wildid.entity.Organization;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDOrganizationTree extends TreeView implements LanguageChangable {

    private LanguageModel language;
    private ObservableList<Organization> orgs;
    private OrganizationListTreeItem rootItem;

    public WildIDOrganizationTree(
            LanguageModel language,
            ObservableList<Organization> orgs,
            OrganizationListTreeItem rootItem) {

        super(rootItem);
        this.rootItem = rootItem;
        this.rootItem.setExpanded(true);

        this.language = language;
        this.orgs = orgs;

        for (Organization org : orgs) {
            rootItem.getChildren().add(new OrganizationTreeItem(org));
        }

        this.setStyle("-fx-background-color:gray;");

        this.requestFocus();
        this.getSelectionModel().select(rootItem);

    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.rootItem.setLanguage(language);
    }

    public void addNewOrganization(Organization org) {

        OrganizationTreeItem orgTreeItem = new OrganizationTreeItem(org);
        ObservableList items = rootItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            OrganizationTreeItem pTreeItem = (OrganizationTreeItem) items.get(i);
            Organization itemOrg = pTreeItem.getOrganization();
            if (org.getOrganizationId().intValue() != itemOrg.getOrganizationId().intValue()) {
                String displayName = org.getName();
                String itemName = itemOrg.getName();
                if (displayName.compareTo(itemName) < 0) {
                    items.add(i, orgTreeItem);
                    added = true;
                    break;
                }
            } else {
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(orgTreeItem);
        }

        this.requestFocus();
        this.getSelectionModel().select(orgTreeItem);
    }

    public void updateOrganization(Organization org) {

        ObservableList items = rootItem.getChildren();
        OrganizationTreeItem orgTreeItem = null;
        for (Object item : items) {
            orgTreeItem = (OrganizationTreeItem) item;
            Organization p = orgTreeItem.getOrganization();
            if (p.getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                orgTreeItem.setOrganization(org);
                break;
            }
        }

        this.requestFocus();
        this.getSelectionModel().select(orgTreeItem);

    }

    public void removeOrganization(Organization org) {

        ObservableList items = rootItem.getChildren();
        OrganizationTreeItem orgTreeItem;
        for (Object item : items) {
            orgTreeItem = (OrganizationTreeItem) item;
            Organization p = orgTreeItem.getOrganization();
            if (p.getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                rootItem.getChildren().remove(orgTreeItem);
                break;
            }
        }

        this.requestFocus();
        this.getSelectionModel().select(rootItem);
    }

}
