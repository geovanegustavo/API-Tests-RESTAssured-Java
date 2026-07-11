package apitests.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {
    private String idProduto;
    private Integer quantidade;

    public CartItem() {
    }

    public CartItem(String idProduto, Integer quantidade) {
        this.idProduto = idProduto;
        this.quantidade = quantidade;
    }

    public String getIdProduto() { return idProduto; }
    public void setIdProduto(String idProduto) { this.idProduto = idProduto; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}