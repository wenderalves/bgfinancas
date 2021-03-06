/*
Copyright 2012-2015 Jose Robson Mariano Alves

This file is part of bgfinancas.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This package is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

package badernageral.bgfinancas.biblioteca.utilitario;

import badernageral.bgfinancas.biblioteca.contrato.Categoria;
import badernageral.bgfinancas.biblioteca.contrato.Controlador;
import badernageral.bgfinancas.biblioteca.contrato.ControladorFiltro;
import badernageral.bgfinancas.biblioteca.contrato.Item;
import badernageral.bgfinancas.idioma.Linguagem;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public final class AutoCompletarTextField<T extends Categoria> extends TextField {

    private final ContextMenu popup;
    private final ObservableList<T> itens;
    private final ObservableList<T> lista_itens;
    private final Controlador controlador;
    private final ControladorFiltro controladorF;

    public AutoCompletarTextField(Controlador _controlador, ControladorFiltro _controladorF) {
        super();
        popup = new ContextMenu();
        itens = FXCollections.observableArrayList();
        lista_itens = FXCollections.observableArrayList();
        controlador = _controlador;
        controladorF = _controladorF;
        setPromptText(Linguagem.getInstance().getMensagem("autofiltro"));
        getStyleClass().add("Filtro");
        textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (getText().length() == 0) {
                popup.hide();
            } else {
                lista_itens.clear();
                String textoConsultado = Outros.removerAcentos(getText().toLowerCase());
                itens.stream().filter(item -> Outros.removerAcentos(item.toString().toLowerCase()).contains(textoConsultado)).forEach(item -> {
                    lista_itens.add(item);
                });
                if (lista_itens.size() > 0) {
                    lista_itens.sort((a, b)  -> {
                        Item iA = (Item) a;
                        Item iB = (Item) b;
                        String nomeA = Outros.removerAcentos(iA.getNome().toLowerCase());
                        String nomeB = Outros.removerAcentos(iB.getNome().toLowerCase());
                        if(nomeA.contains(textoConsultado) && !nomeB.contains(textoConsultado)) { return -1; }
                        if(!nomeA.contains(textoConsultado) && nomeB.contains(textoConsultado)) { return 1; }
                        if(nomeA.startsWith(textoConsultado) && !nomeB.startsWith(textoConsultado)) { return -1; }
                        if(!nomeA.startsWith(textoConsultado) && nomeB.startsWith(textoConsultado)) { return 1; }
                        return Outros.removerAcentos(iA.toString()).compareTo(Outros.removerAcentos(iB.toString()));
                    });
                    popularPopup(lista_itens);
                    if (!popup.isShowing()) {
                        popup.show(AutoCompletarTextField.this, Side.BOTTOM, 0, 0);
                    }
                } else {
                    popup.hide();
                }
            }
        });
        focusedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) -> {
            popup.hide();
        });
        if(controlador!=null){
            setOnKeyReleased(event -> {
                if(event.getCode()==KeyCode.ENTER){
                    if(!getText().equals("")){
                        controlador.acaoCadastrar(1);
                    }
                }
            });
        }
    }

    public void setItens(ObservableList<T> _itens) {
        itens.setAll(_itens);
    }

    private void popularPopup(ObservableList<T> lista_itens) {
        int maximoItens = 18;
        int contador = Math.min(lista_itens.size(), maximoItens);
        List<CustomMenuItem> popupItens = new LinkedList<>();
        for (int i = 0; i < contador; i++) {
            final T item = lista_itens.get(i);
            Label itemLabel = new Label(item.toString());
            CustomMenuItem menuItem = new CustomMenuItem(itemLabel, true);
            menuItem.setOnAction(actionEvent -> {
                setText(item.toString());
                popup.hide();
                if(controlador!=null){
                    controladorF.adicionar(null);
                }
            });
            popupItens.add(menuItem);
        }
        popup.getItems().clear();
        popup.getItems().addAll(popupItens);
    }

}
