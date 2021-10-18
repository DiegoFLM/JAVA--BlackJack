package comunes;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Carta extends JLabel implements Serializable{
    private String valor;
    private String palo;
    private BufferedImage imagen;
 	
    public Carta(String valor, String palo) {
		this.valor = valor;
		this.palo = palo;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getPalo() {
		return palo;
	}

	public void setPalo(String palo) {
		this.palo = palo;
	}
	
	public String toString() {
		return valor+palo;
	}
	
	public void setImagen(BufferedImage imagen) {
		this.imagen = imagen;
		setIcon(new ImageIcon(imagen));
	}
	
	public BufferedImage getImagen() {
		return imagen;
	}
	
}
