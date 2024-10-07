package com.example.entregable1;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProductAdapter adapter;
    private List<Product> products;
    private List<Product> filteredProducts;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.product_list);
        EditText searchBar = findViewById(R.id.search_bar);
        Button addButton = findViewById(R.id.add_button);  // Botón de agregar producto

        dbHelper = new DatabaseHelper(this);
        products = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        adapter = new ProductAdapter(this, filteredProducts);
        listView.setAdapter(adapter);

        // Cargar productos desde la base de datos
        fetchProducts();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterProducts(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = filteredProducts.get(position);
                showProductDialog(selectedProduct);
            }
        });

        // Manejar clic en el botón de agregar producto
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProductDialog();
            }
        });
    }

    private void fetchProducts() {
        products.clear();
        products.addAll(dbHelper.getAllProducts());
        filteredProducts.clear();
        filteredProducts.addAll(products);
        adapter.notifyDataSetChanged();
    }

    private void filterProducts(String query) {
        filteredProducts.clear();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredProducts.add(product);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Options");

        String[] options = {"Edit", "Delete"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Editar producto
                    showEditProductDialog(product);
                } else {
                    // Eliminar producto
                    deleteProduct(product);
                }
            }
        });
        builder.show();
    }

    private void showEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Product");

        View view = getLayoutInflater().inflate(R.layout.dialog_product, null);
        EditText productNameInput = view.findViewById(R.id.product_name_input);
        EditText productDescriptionInput = view.findViewById(R.id.product_description_input);
        EditText productCategoryInput = view.findViewById(R.id.product_category_input);

        // Establecer los campos con la información del producto
        productNameInput.setText(product.getName());
        productDescriptionInput.setText(product.getDescription());
        productCategoryInput.setText(product.getCategory());

        builder.setView(view);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Crear un nuevo producto con la información editada
                String name = productNameInput.getText().toString();
                String description = productDescriptionInput.getText().toString();
                String category = productCategoryInput.getText().toString();

                // Usar el mismo ID del producto original
                Product updatedProduct = new Product(product.getId(), name, description, category);
                dbHelper.updateProduct(updatedProduct);
                fetchProducts();
                Toast.makeText(MainActivity.this, "Product updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        View view = getLayoutInflater().inflate(R.layout.dialog_product, null);
        EditText productNameInput = view.findViewById(R.id.product_name_input);
        EditText productDescriptionInput = view.findViewById(R.id.product_description_input);
        EditText productCategoryInput = view.findViewById(R.id.product_category_input);

        builder.setView(view);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Crear un nuevo producto
                String name = productNameInput.getText().toString();
                String description = productDescriptionInput.getText().toString();
                String category = productCategoryInput.getText().toString();

                // Insertar nuevo producto en la base de datos
                dbHelper.addProduct(name, description, category);
                fetchProducts(); // Actualizar la lista después de agregar
                Toast.makeText(MainActivity.this, "Product added", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteProduct(product.getId());
                        fetchProducts();
                        Toast.makeText(MainActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
