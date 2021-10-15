package clientebj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import comunes.DatosBlackJack;

public class VentanaSalaJuego extends JInternalFrame {
	    
		private PanelJugador dealer, yo, jugador2, jugador3;
		private JTextArea areaMensajes;
		private JButton pedir, plantar;
		private JPanel panelYo, panelBotones, yoFull, panelDealer,panelJugador2, panelJugador3;
		
		private String yoId, jugador2Id, jugador3Id;
		//private DatosBlackJack datosRecibidos;
		private Escucha escucha;
		
		private GridBagConstraints constraints;
		
		public VentanaSalaJuego(String yoId, String jugador2Id, String jugador3Id) {
			this.yoId = yoId;
			this.jugador2Id = jugador2Id;
			this.jugador3Id = jugador3Id;
			//this.datosRecibidos=datosRecibidos;
						
			initGUI();
			
			//default window settings
			this.setTitle("Sala de juego BlackJack - Jugador: "+yoId);
			this.pack();
			this.setLocation((ClienteBlackJack.WIDTH-this.getWidth())/2, 
			         (ClienteBlackJack.HEIGHT-this.getHeight())/2);
			this.setResizable(false);
			this.show();
		}

		private void initGUI() {
			// TODO Auto-generated method stub
			//set up JFrame Container y Layout
	        
			//Create Listeners objects
			escucha = new Escucha();
			//Create Control objects
						
			//Set up JComponents
			this.setLayout(new GridBagLayout());
			constraints = new GridBagConstraints();
			
			
			
			
			panelDealer = new JPanel();
			dealer = new PanelJugador("Dealer");
			panelDealer.add(dealer);
			
			constraints.gridx = 0;		
	 		constraints.gridy = 0;
			constraints.gridwidth = 4;
			constraints.gridheight = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			add(panelDealer, constraints);
			//add(panelDealer,BorderLayout.NORTH);		
			
			
			panelJugador2 = new JPanel();
			jugador2= new PanelJugador(jugador2Id);	
			panelJugador2.add(jugador2);
			
			constraints.gridx = 2;		
	 		constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			add(panelJugador2, constraints);
			//add(panelJugador2,BorderLayout.EAST);	
			
			panelJugador3 = new JPanel();
			jugador3= new PanelJugador(jugador3Id);	
			panelJugador2.add(jugador3);
			
			constraints.gridx = 3;		
	 		constraints.gridy =1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			add(panelJugador3, constraints);
			//add(panelJugador3, a la derecha de panelJugador2);
			
			
			
			areaMensajes = new JTextArea(8,18);
			JScrollPane scroll = new JScrollPane(areaMensajes);	
			Border blackline;
			blackline = BorderFactory.createLineBorder(Color.black);
			TitledBorder bordes;
			bordes = BorderFactory.createTitledBorder(blackline, "Area de Mensajes");
	        bordes.setTitleJustification(TitledBorder.CENTER);
			scroll.setBorder(bordes);
			areaMensajes.setOpaque(false);
			areaMensajes.setBackground(new Color(0, 0, 0, 0));
			areaMensajes.setEditable(false);

			scroll.getViewport().setOpaque(false);
			scroll.setOpaque(false);
			
			constraints.gridx = 1;		
	 		constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			add(scroll, constraints);
			//add(scroll,BorderLayout.CENTER);
			
			
			panelYo = new JPanel();
			panelYo.setLayout(new BorderLayout());
			yo = new PanelJugador(yoId);
			panelYo.add(yo);
				
			pedir = new JButton("Carta");
			pedir.setEnabled(false);
			pedir.addActionListener(escucha);
			plantar = new JButton("Plantar");
			plantar.setEnabled(false);
			plantar.addActionListener(escucha);
			panelBotones = new JPanel();
			panelBotones.add(pedir);
			panelBotones.add(plantar);
			
			yoFull = new JPanel();
			yoFull.setPreferredSize(new Dimension(206,100));
			yoFull.add(panelYo);
			yoFull.add(panelBotones);
			
			constraints.gridx = 0;		
	 		constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			add(yoFull, constraints);
			//add(yoFull,BorderLayout.WEST);	
		}
		
		public void activarBotones(boolean turno) {
			pedir.setEnabled(turno);
			plantar.setEnabled(turno);
		}
		
		public void pintarCartasInicio(DatosBlackJack datosRecibidos) {
			if(datosRecibidos.getIdJugadores()[0].equals(yoId)) {
				yo.pintarCartasInicio(datosRecibidos.getManoJugador1());
				if (datosRecibidos.getIdJugadores()[1].equals(jugador2Id)) {
					jugador2.pintarCartasInicio(datosRecibidos.getManoJugador2());
					jugador3.pintarCartasInicio(datosRecibidos.getManoJugador3());
				} else {
					jugador2.pintarCartasInicio(datosRecibidos.getManoJugador3());
					jugador3.pintarCartasInicio(datosRecibidos.getManoJugador2());
				}
			}else if(datosRecibidos.getIdJugadores()[0].equals(jugador2Id)) {
				jugador2.pintarCartasInicio(datosRecibidos.getManoJugador1());
				if (datosRecibidos.getIdJugadores()[1].equals(yoId)) {
					yo.pintarCartasInicio(datosRecibidos.getManoJugador2());
					jugador3.pintarCartasInicio(datosRecibidos.getManoJugador3());
				} else {
					yo.pintarCartasInicio(datosRecibidos.getManoJugador3());
					jugador3.pintarCartasInicio(datosRecibidos.getManoJugador2());
				}
			} else {
				jugador3.pintarCartasInicio(datosRecibidos.getManoJugador1());
				if (datosRecibidos.getIdJugadores()[1].equals(yoId)) {
					yo.pintarCartasInicio(datosRecibidos.getManoJugador2());
					jugador2.pintarCartasInicio(datosRecibidos.getManoJugador3());
				} else {
					yo.pintarCartasInicio(datosRecibidos.getManoJugador3());
					jugador2.pintarCartasInicio(datosRecibidos.getManoJugador2());
				}
			}
			dealer.pintarCartasInicio(datosRecibidos.getManoDealer());
			areaMensajes.append(datosRecibidos.getMensaje()+"\n");
		}
				
				
		/*				
				jugador2.pintarCartasInicio(datosRecibidos.getManoJugador2());
			}else {
				yo.pintarCartasInicio(datosRecibidos.getManoJugador2());
				jugador2.pintarCartasInicio(datosRecibidos.getManoJugador1());
			}
			dealer.pintarCartasInicio(datosRecibidos.getManoDealer());
			
			areaMensajes.append(datosRecibidos.getMensaje()+"\n");
		}*/
		
		public void pintarTurno(DatosBlackJack datosRecibidos) {
			areaMensajes.append(datosRecibidos.getMensaje()+"\n");	
			ClienteBlackJack cliente = (ClienteBlackJack)this.getTopLevelAncestor();
			
			if(datosRecibidos.getJugador().contentEquals(yoId)){
				if(datosRecibidos.getJugadorEstado().equals("iniciar")) {
					activarBotones(true);
				}else {
					if(datosRecibidos.getJugadorEstado().equals("plant�") ){
						cliente.setTurno(false);
					}else {
						yo.pintarLaCarta(datosRecibidos.getCarta());
						if(datosRecibidos.getJugadorEstado().equals("vol�")) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									activarBotones(false);
									cliente.setTurno(false);
								}});			
						      }
						}
					} 
			 }else {//movidas de los otros jugadores
				 
				 	//Movida del jugador2
					if(datosRecibidos.getJugador().equals(jugador2Id)) {
						//mensaje para PanelJuego jugador2
						if(datosRecibidos.getJugadorEstado().equals("sigue")||
						   datosRecibidos.getJugadorEstado().equals("vol�")) {
							jugador2.pintarLaCarta(datosRecibidos.getCarta());
						}
						
					//Movida del jugador3	
					}else if(datosRecibidos.getJugador().equals(jugador3Id)) {
						//mensaje para PanelJuego jugador3
						if(datosRecibidos.getJugadorEstado().equals("sigue")||
								   datosRecibidos.getJugadorEstado().equals("vol�")) {
									jugador3.pintarLaCarta(datosRecibidos.getCarta());
								}
						
					}else {
						//mensaje para PanelJuego dealer
						if(datosRecibidos.getJugadorEstado().equals("sigue") ||
						   datosRecibidos.getJugadorEstado().equals("vol�")	||
						   datosRecibidos.getJugadorEstado().equals("plant�")) {
							dealer.pintarLaCarta(datosRecibidos.getCarta());
						}
					}
				}			 	
		}		
	   
	   private void enviarDatos(String mensaje) {
			// TODO Auto-generated method stub
		  ClienteBlackJack cliente = (ClienteBlackJack)this.getTopLevelAncestor();
		  cliente.enviarMensajeServidor(mensaje);
		}
		   
	  
	   private class Escucha implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO Auto-generated method stub
			if(actionEvent.getSource()==pedir) {
				//enviar pedir carta al servidor
				enviarDatos("pedir");				
			}else {
				//enviar plantar al servidor
				enviarDatos("plantar");
				activarBotones(false);
			}
		}
	   }
}
