package apitests.models;

import java.util.List;

public class Cart {
    private List<CartItem> produtos;

    public Cart(List<CartItem> produtos) {
        this.produtos = produtos;
    }

    public List<CartItem> getProdutos() { return produtos; }
    public void setProdutos(List<CartItem> produtos) { this.produtos = produtos; }
}