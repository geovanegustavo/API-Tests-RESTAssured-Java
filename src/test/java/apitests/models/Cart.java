package apitests.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart {
    private List<CartItem> produtos;

    public Cart() {
    }

    public Cart(List<CartItem> produtos) {
        this.produtos = produtos;
    }

    public List<CartItem> getProdutos() { return produtos; }
    public void setProdutos(List<CartItem> produtos) { this.produtos = produtos; }
}