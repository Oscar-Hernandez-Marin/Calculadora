package com.example.calculadora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText txtResultat;
    private boolean iniciarNuevoNumero = true;

    private double valorMemoria = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtResultat = (EditText) findViewById(R.id.txtResult);
        txtResultat.setEnabled(false);

        String[] botonNumerics = {"btnNum1","btnNum2","btnNum3","btnNum4","btnNum5","btnNum6","btnNum7","btnNum8","btnNum9","btnNum0","btnComa"};
        for (String id : botonNumerics) {
            Button btn = findViewById(getResources().getIdentifier(id,"id",getPackageName()));
            btn.setOnClickListener(v -> numeroPresionado(btn));
        }
        findViewById(R.id.btnSuma).setOnClickListener(v -> prepararOperacion("+"));
        findViewById(R.id.btnResta).setOnClickListener(v -> prepararOperacion("-"));
        findViewById(R.id.btnMultiplicacio).setOnClickListener(v -> prepararOperacion("*"));
        findViewById(R.id.btnDivisio).setOnClickListener(v -> prepararOperacion("/"));
        findViewById(R.id.btnIgual).setOnClickListener(v -> calcularResultado());

        Button btnMC = findViewById(R.id.btnMc);
        Button btnMR = findViewById(R.id.btnMr);
        Button btnMPlus = findViewById(R.id.btnMPlus);
        Button btnMMinus = findViewById(R.id.btnMMinus);
        Button btnDEL = findViewById(R.id.btnDel);

        btnMPlus.setOnClickListener(v -> {
            double numeroPantalla = obtenirNumeroDePantalla();
            valorMemoria += numeroPantalla;
            Toast.makeText(this, "Sumat a la memòria: " + formatarResultat(valorMemoria), Toast.LENGTH_SHORT).show();
            iniciarNuevoNumero = true;
        });

        btnMMinus.setOnClickListener(v -> {
            double numeroPantalla = obtenirNumeroDePantalla();
            valorMemoria -= numeroPantalla;
            Toast.makeText(this, "Restat de la memòria", Toast.LENGTH_SHORT).show();
            iniciarNuevoNumero = true;
        });

        btnMR.setOnClickListener(v -> {
            String textMemoria = formatarResultat(valorMemoria);
            ajustarTextoPantalla(textMemoria);
            iniciarNuevoNumero = true;
        });

        btnMC.setOnClickListener(v -> {
            valorMemoria = 0.0;
            Toast.makeText(this, "Memòria esborrada", Toast.LENGTH_SHORT).show();
        });

        btnDEL.setOnClickListener(v -> eliminarNum());
    }
    public void eliminarNum() {
        String textoActual = txtResultat.getText().toString();

        if (!textoActual.isEmpty()) {
            txtResultat.setText(textoActual.substring(0,textoActual.length()-1));
        }
    }

    public void numeroPresionado(View view) {
        Button boton = (Button) view;
        String digito = boton.getText().toString();
        String textoActual = txtResultat.getText().toString();

        if (digito.equals(".")) {
            if (iniciarNuevoNumero || textoActual.equals("0") || textoActual.isEmpty()) {
                txtResultat.setText("0.");
                iniciarNuevoNumero = false;
                txtResultat.setSelection(txtResultat.getText().length());
                return;
            }
            String[] partes = textoActual.split(" ");
            String ultimoNumero = partes[partes.length - 1];

            if (ultimoNumero.contains(",")) {
                return;
            }
        }

        if (iniciarNuevoNumero) {
            txtResultat.setText(digito);
            iniciarNuevoNumero = false;
        } else {
            if (textoActual.equals("0") && !digito.equals(",")) {
                txtResultat.setText(digito);
            } else {
                txtResultat.append(digito);
            }
        }
        txtResultat.setSelection(txtResultat.getText().length());
    }

    private void ajustarTextoPantalla(String texto) {
        txtResultat.setText(texto);
        txtResultat.setSelection(txtResultat.getText().length());
    }

    private void prepararOperacion(String operacion) {
        String textoActual = txtResultat.getText().toString();

        if (textoActual.equals("Error")) return;

        if (iniciarNuevoNumero) {
            iniciarNuevoNumero = false;
        }

        if (!textoActual.isEmpty() && !textoActual.endsWith(" ")) {
            txtResultat.append(" " + operacion + " ");
        }
    }

    private void calcularResultado() {
        String textCompletet = txtResultat.getText().toString().trim();

        if (textCompletet.isEmpty() || textCompletet.equals("Error")) return;

        String[] valores = textCompletet.split(" ");

        if (valores.length < 3) return;

        double resultat = 0.0;
        try {
            resultat = Double.parseDouble(valores[0]);
        } catch (NumberFormatException e) {
            ajustarTextoPantalla("Error");
            iniciarNuevoNumero = true;
            return;
        }

        for (int i = 1; i < valores.length; i += 2) {
            if (i + 1 >= valores.length) break;

            String operador = valores[i];
            double siguienteNumero;

            try {
                siguienteNumero = Double.parseDouble(valores[i + 1]);
            } catch (NumberFormatException e) {
                ajustarTextoPantalla("Error");
                iniciarNuevoNumero = true;
                return;
            }
            switch (operador) {
                case "+":
                    resultat += siguienteNumero;
                    break;
                case "-":
                    resultat -= siguienteNumero;
                    break;
                case "*":
                    resultat *= siguienteNumero;
                    break;
                case "/":
                    if (siguienteNumero == 0.0) {
                        Toast.makeText(this, "No se puede dividir entre cero", Toast.LENGTH_SHORT).show();
                        ajustarTextoPantalla("Error");
                        iniciarNuevoNumero = true;
                        return;
                    }
                    resultat /= siguienteNumero;
                    break;
                default:
                    break;
            }
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String resultatStr;
        if (resultat % 1 == 0.0) {
            resultatStr = String.valueOf((int) resultat);
        } else {
            resultatStr = df.format(resultat);
        }

        ajustarTextoPantalla(resultatStr);
        iniciarNuevoNumero = true;
    }

    private double obtenirNumeroDePantalla() {
        String text = txtResultat.getText().toString().trim();
        if (text.isEmpty() || text.equals("Error")) return 0.0;

        text = text.replace(",", ".");
        String[] valores = text.split(" ");

        String ultimValor = valores[valores.length - 1];
        try {
            return Double.parseDouble(ultimValor);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    private String formatarResultat(double numero) {
        if (numero % 1 == 0.0) {
            return String.valueOf((int) numero);
        } else {
            return String.valueOf(numero).replace(".", ",");
        }
    }
}