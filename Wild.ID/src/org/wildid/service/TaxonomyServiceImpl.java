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
package org.wildid.service;

import java.util.List;
import org.wildid.dao.TaxonomyServiceDAO;
import org.wildid.dao.TaxonomyServiceDAOImpl;
import org.wildid.entity.FamilyGenusSpecies;
import org.wildid.entity.HomoSapiensType;
import org.wildid.entity.TaxaCommonEnglishName;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class TaxonomyServiceImpl implements TaxonomyService {

    private TaxonomyServiceDAO taxaDAO = new TaxonomyServiceDAOImpl();

    @Override
    public List<String> getGenusSuggestion(String string) {
        return taxaDAO.getGenusSuggestion(string);
    }

    @Override
    public List<String> getSpeciesSuggestion(String genus, String string) {
        return taxaDAO.getSpeciesSuggestion(genus, string);
    }

    @Override
    public List<String> getCommonNameSuggestion(String string) {
        return taxaDAO.getCommonNameSuggestion(string);
    }

    @Override
    public FamilyGenusSpecies getFamilyGenusSpecies(String genus, String species) {
        return taxaDAO.getFamilyGenusSpecies(genus, species);
    }

    @Override
    public void addHomoSapiensType(HomoSapiensType sapiensType) {
        taxaDAO.addHomoSapiensType(sapiensType);
    }

    @Override
    public List<HomoSapiensType> listHomoSapiensType() {
        return taxaDAO.listHomoSapiensType();
    }

    @Override
    public void removeHomoSapiensType(Integer id) {
        taxaDAO.removeHomoSapiensType(id);
    }

    @Override
    public void updateHomoSapiensType(HomoSapiensType sapiensType) {
        taxaDAO.updateHomoSapiensType(sapiensType);
    }

    @Override
    public List<TaxaCommonEnglishName> getCommonEnglishNames(FamilyGenusSpecies fgs) {
        return taxaDAO.getCommonEnglishNames(fgs);
    }

    @Override
    public TaxaCommonEnglishName getCommonEnglishNames(String commonName) {
        return taxaDAO.getCommonEnglishNames(commonName);
    }

    @Override
    public List<String> loadUsedGenus() {
        return taxaDAO.loadUsedGenus();
    }

    @Override
    public List<String> loadUsedSpecies(String genus) {
        return taxaDAO.loadUsedSpecies(genus);
    }

    @Override
    public List<String> loadUsedCommonNames() {
        return taxaDAO.loadUsedCommonNames();
    }

}
