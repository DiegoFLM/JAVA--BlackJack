package clientebj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import comunes.Carta;

public class PanelJugador extends JPanel {
	//constantes de clase
	private static final int ANCHO = 206;
	private static final int ALTO = 89;
	public static final String CARTAS_FILE="/resources/cards.png";
	private static final int PALOS=4;
	private static final int VALORES=13;
	public static final int CARTA_WIDTH=45;
	public static final int CARTA_HEIGHT=60;
	
	
	//variables para control del graficado
	private ArrayList<Recuerdo> dibujoRecordar;
	private int x;
	    
	public PanelJugador(String nombreJugador) {
		//this.setBackground(Color.GREEN);
		dibujoRecordar = new ArrayList<Recuerdo>();
		this.setPreferredSize(new Dimension(ANCHO,ALTO));
		TitledBorder bordes;
		
		if (nombreJugador.equals("Dealer")){//*added apuestas
			bordes = BorderFactory.createTitledBorder(nombreJugador);
			//JOptionPane.showMessageDialog(null, nombreJugador);//*added apuestas
		}else {//*added apuestas
			bordes = BorderFactory.createTitledBorder(nombreJugador.substring(0, firstNumIndex(nombreJugador)) );//*added apuestas
		}
		
		this.setBorder(bordes);
	}
	
	private void asignarImagen(Carta carta) {
		BufferedImage cardsImage = FileIO.readImageFile(this, CARTAS_FILE);
		int x = 0;
		int y = 0;
		if(carta.getValor().equals("As")) {
			x = 12;
		} else if(carta.getValor().equals("J")) {
			x = 9;
		}
		else if(carta.getValor().equals("Q")) {
			x = 10;
		}
		else if(carta.getValor().equals("K")) {
			x = 11;
		} else {
			x = Integer.parseInt(carta.getValor())-2;
		}
		
		if(carta.getPalo().equals("C")) {
			y = 0;
		}
		else if(carta.getPalo().equals("D")) {
			y = 1;
		}
		else if(carta.getPalo().equals("P")) {
			y = 2;
		} else {
			y = 3;
		}
		
		carta.setImagen(cardsImage.getSubimage((x*CARTA_WIDTH), (y*CARTA_HEIGHT), CARTA_WIDTH, CARTA_HEIGHT));
	}
	
	public void pintarCartasInicio(ArrayList<Carta> manoJugador) {
		x=5;
	    for(int i=0;i<manoJugador.size();i++) {
	    	asignarImagen(manoJugador.get(i));
	    	dibujoRecordar.add(new Recuerdo(manoJugador.get(i),x));
	    	x+=27;
	    }			
	    repaint();
	}
	
	public void pintarLaCarta (Carta carta) {
		asignarImagen(carta);
		dibujoRecordar.add(new Recuerdo(carta,x));
		x+=27;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Font font =new Font(Font.DIALOG,Font.BOLD,12);
		g.setFont(font);
		
		int index = 1;
		//pinta la mano inicial
		for(int i=0;i<dibujoRecordar.size();i++) {
			g.drawImage(dibujoRecordar.get(i).getCartaImage(),dibujoRecordar.get(i).getxRecordar(),16,this);
			//g.drawString(dibujoRecordar.get(i).getCartaRecordar(), dibujoRecordar.get(i).getxRecordar(),35);
			index++;
		}	
	}
	
	//*added apuestas
	private int firstNumIndex(String s){
	    for (int i=0; i<s.length(); i++)
	    {
	        if (Character.isDigit(s.charAt(i)))
	            return i;
	    }
	    return -1;
	  }
	
	private int extraerApuesta(String ident) {
		int index0 = firstNumIndex(ident);
		int apsta =Integer.valueOf(ident.substring(index0, ident.length()));
		return apsta;
	}
	
	private String[] strArray (int[] intArr){
		String[] ans = new String[intArr.length];
		for (int q = 0; q < intArr.length; q++) {
			ans[q] = String.valueOf(intArr[q]);
		}
		return ans; 
	}
	
	private String[] idsSinApuesta(String[] idsConApu) {
		String[] ans = new String[idsConApu.length];
		
		for (int j = 0; j < 3; j++) {
			ans[j] = idsConApu[j].substring(0, firstNumIndex(idsConApu[j]));
		}
		
		return ans;
	}
	
	//*
	
	private class Recuerdo{
		private Carta cartaRecordar;
		private int xRecordar;

		public Recuerdo(Carta cartaRecordar, int xRecordar) {
			this.cartaRecordar = cartaRecordar;
			this.xRecordar = xRecordar;
		}
		
		public BufferedImage getCartaImage() {
			return cartaRecordar.getImagen();
		}

		public String getCartaRecordar() {
			return cartaRecordar.toString();
		}

		public int getxRecordar() {
			return xRecordar;
		}
	}

}
