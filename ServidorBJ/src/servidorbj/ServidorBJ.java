package servidorbj;

import java.io.Console;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.midi.VoiceStatus;
import javax.swing.JOptionPane;

import comunes.Baraja;
import comunes.Carta;
import comunes.DatosBlackJack;

/* Clase encargada de realizar la gestión del juego, esto es, el manejo de turnos y estado del juego.
 * También gestiona al jugador Dealer. 
 * El Dealer tiene una regla de funcionamiento definida:
 * Pide carta con 16 o menos y Planta con 17 o mas.
 */
public class ServidorBJ implements Runnable{
	//constantes para manejo de la conexion.
	public static final int PUERTO=7377;
	public static final String IP="127.0.0.1";
	public static final int LONGITUD_COLA=3;

	// variables para funcionar como servidor
	private ServerSocket server;
	private Socket conexionJugador;
	
	//variables para manejo de hilos
	private ExecutorService manejadorHilos;
	private Lock bloqueoJuego;
	private Condition esperarInicio, esperarTurno, finalizar;
	private Jugador[] jugadores;
	
	//variables de control del juego
	private String[] idJugadores;
	private String[] idJugadoresSinApu;
	
	private int jugadorEnTurno;
	//private boolean iniciarJuego;
	private Baraja mazo;
	private ArrayList<ArrayList<Carta>> manosJugadores;
	private ArrayList<Carta> manoJugador1;
	private ArrayList<Carta> manoJugador2;
	private ArrayList<Carta> manoJugador3;
	private ArrayList<Carta> manoDealer;
	private int[] valorManos;
	private DatosBlackJack datosEnviar;
	
	//private String[] apuestasIniciales; //* added apuestas
	private int[] apuestasIniciales; //*added apuestas
	private int[] apuestas; //* added apuestas
	
	public ServidorBJ() {
	    //inicializar variables de control del juego
		inicializarVariablesControlRonda();
	    //inicializar las variables de manejo de hilos
		inicializareVariablesManejoHilos();
		//crear el servidor
    	try {
    		mostrarMensaje("Iniciando el servidor...");
			server = new ServerSocket(PUERTO,LONGITUD_COLA);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	
    private void inicializareVariablesManejoHilos() {
		// TODO Auto-generated method stub
    	manejadorHilos = Executors.newFixedThreadPool(LONGITUD_COLA);
		bloqueoJuego = new ReentrantLock();
		esperarInicio = bloqueoJuego.newCondition();
		esperarTurno = bloqueoJuego.newCondition();
		finalizar = bloqueoJuego.newCondition();
		jugadores = new Jugador[LONGITUD_COLA];	
	}

	private void inicializarVariablesControlRonda() {
		// TODO Auto-generated method stub
    	 //Variables de control del juego.
		
		idJugadores = new String[3];
		valorManos = new int[4];
		
		mazo = new Baraja();
		Carta carta;
		
		idJugadoresSinApu = new String[3];//*added apuestas
		//apuestasIniciales = new String[3];	//*added apuestas
		apuestas = new int[3]; 	//*added apuestas
		apuestasIniciales = new int[3];	//*added apuestas
		
		
		
		
		manoJugador1 = new ArrayList<Carta>();
		manoJugador2 = new ArrayList<Carta>();
		manoJugador3 = new ArrayList<Carta>();
		manoDealer = new ArrayList<Carta>();
		
		//reparto inicial jugadores 1, 2 y 3, se le reparten 2 cartas a cada jugador y una carta al dealer.
		for(int i=1;i<=2;i++) {
		  carta = mazo.getCarta();
		  manoJugador1.add(carta);
		  calcularValorMano(carta,0);
		  carta = mazo.getCarta();
		  manoJugador2.add(carta);
		  calcularValorMano(carta,1);
		  carta = mazo.getCarta();
		  manoJugador3.add(carta);
		  calcularValorMano(carta,2);
		}
		//Carta inicial Dealer
		carta = mazo.getCarta();
		manoDealer.add(carta);
		calcularValorMano(carta,3);
		
		//gestiona las tres manos en un solo objeto para facilitar el manejo del hilo
		manosJugadores = new ArrayList<ArrayList<Carta>>(4);
		manosJugadores.add(manoJugador1);
		manosJugadores.add(manoJugador2);
		manosJugadores.add(manoJugador3);
		manosJugadores.add(manoDealer);
	}
	
	
	//*added restart
	//*added restart
	public void restart() {
		
		//inicializarVariablesControlRonda();
		////////////***inicializarVaciables
		jugadorEnTurno = 0;
		valorManos = new int[4];
		
		mazo = new Baraja();
		Carta carta;
		
		manoJugador1 = new ArrayList<Carta>();
		manoJugador2 = new ArrayList<Carta>();
		manoJugador3 = new ArrayList<Carta>();
		manoDealer = new ArrayList<Carta>();
		
		for(int i=1;i<=2;i++) {
			  carta = mazo.getCarta();
			  manoJugador1.add(carta);
			  calcularValorMano(carta,0);
			  carta = mazo.getCarta();
			  manoJugador2.add(carta);
			  calcularValorMano(carta,1);
			  carta = mazo.getCarta();
			  manoJugador3.add(carta);
			  calcularValorMano(carta,2);
			}
			//Carta inicial Dealer
			carta = mazo.getCarta();
			manoDealer.add(carta);
			calcularValorMano(carta,3);
			
			//gestiona las tres manos en un solo objeto para facilitar el manejo del hilo
			manosJugadores = new ArrayList<ArrayList<Carta>>(4);
			manosJugadores.add(manoJugador1);
			manosJugadores.add(manoJugador2);
			manosJugadores.add(manoJugador3);
			manosJugadores.add(manoDealer);
		////////////***
		
		
		
		
		datosEnviar = new DatosBlackJack();
		datosEnviar.setManoDealer(manosJugadores.get(3));
		datosEnviar.setManoJugador1(manosJugadores.get(0));
		datosEnviar.setManoJugador2(manosJugadores.get(1));	
		datosEnviar.setManoJugador3(manosJugadores.get(2));		
		datosEnviar.setIdJugadores(idJugadores);
		datosEnviar.setValorManos(valorManos);
		datosEnviar.setJugador(idJugadores[0]);
		datosEnviar.setJugadorEstado("iniciar");
		//datosEnviar.setMensaje("Inicias "+idJugadores[0]+" tienes "+valorManos[0]);
		datosEnviar.setMensaje("Inicias "+idJugadoresSinApu[0]+" tienes "+valorManos[0]);//*added apuestas
		
		
		
		
		//* added apuestas
		apuestasIniciales[0] = extraerApuesta(idJugadores[0]);
		apuestasIniciales[1] = extraerApuesta(idJugadores[1]);
		apuestasIniciales[2] = extraerApuesta(idJugadores[2]);
		
		apuestas = apuestasIniciales;
		
		datosEnviar.setApuestas(strArray(apuestas));
		//*
		
		
		jugadores[0].enviarMensajeCliente(datosEnviar);
		jugadores[1].enviarMensajeCliente(datosEnviar);
		jugadores[2].enviarMensajeCliente(datosEnviar);
		
		//esperarInicio.signalAll();
		jugadores[1].setSuspendido(true);
		
		//analizarMensaje("iniciar", 0);//		ESTA LINEA SE DEBE CAMBIAR.
		
		
		
		
		
		/*iniciarRondaJuego(); //despertar al jugador 1 para iniciar el juego
		
		
		
		
		
		//  /  *
		mostrarMensaje("Bloquea al servidor para poner en espera de turno al jugador 3");
		bloqueoJuego.lock();
		try {
			mostrarMensaje("Pone en espera de turno al jugador 3");
			esperarTurno.await();
			mostrarMensaje("Despierta de la espera de inicio del juego al jugador 1");
            //
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			bloqueoJuego.unlock();
		}
		// *  /
		 */
	}
	//*
	

	private void calcularValorMano(Carta carta, int i) {
		// TODO Auto-generated method stub
    	
			if(carta.getValor().equals("As")) {
				valorManos[i]+=11;
			}else {
				if(carta.getValor().equals("J") || carta.getValor().equals("Q")
						   || carta.getValor().equals("K")) {
					valorManos[i]+=10;
				}else {
					valorManos[i]+=Integer.parseInt(carta.getValor()); 
				}
		}
	}
	
	public void iniciar() {
       	//esperar a los clientes
    	mostrarMensaje("Esperando a los jugadores...");
    	
    	for(int i=0; i<LONGITUD_COLA;i++) {
    		try {
				conexionJugador = server.accept();
				jugadores[i] = new Jugador(conexionJugador,i);
	    		manejadorHilos.execute(jugadores[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
    	} 	
    }
    
	private void mostrarMensaje(String mensaje) {
		System.out.println(mensaje);
	}
	
	private void iniciarRondaJuego() {
		
		this.mostrarMensaje("bloqueando al servidor para despertar al jugador 1");
    	bloqueoJuego.lock();
    	
    	//despertar al jugador 1 porque es su turno
    	try {
    		this.mostrarMensaje("Despertando al jugador 1 para que inicie el juego");
        	jugadores[0].setSuspendido(false);
        	jugadores[1].setSuspendido(false);
        	//jugadores[2].setSuspendido(false);
        	esperarInicio.signalAll();
        	//esperarTurno.signal();
    	}catch(Exception e) {
    		
    	}finally {
    		this.mostrarMensaje("Desbloqueando al servidor luego de despertar al jugador 1 para que inicie el juego");
    		bloqueoJuego.unlock();
    	}			
	}
	
    private boolean seTerminoRonda( boolean seTermino) {
       return seTermino;
    }
    
    private void analizarMensaje(String entrada, int indexJugador) {
		// TODO Auto-generated method stub
        //garantizar que solo se analice la petición del jugador en turno.
    	while(indexJugador!=jugadorEnTurno) {
    		bloqueoJuego.lock();
    		try {
				esperarTurno.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		bloqueoJuego.unlock();
    	}
    	
    	//valida turnos para jugador 0 o 1
        	
    	if(entrada.equals("pedir")) {
    		//dar carta 
    		//mostrarMensaje("Se envió carta al jugador "+ idJugadores[indexJugador]); 
    		mostrarMensaje("Se envió carta al jugador "+ idJugadoresSinApu[indexJugador]);//*added apuestas
    		Carta carta = mazo.getCarta();
    		//adicionar la carta a la mano del jugador en turno
    		manosJugadores.get(indexJugador).add(carta);
    		calcularValorMano(carta, indexJugador);
    		
    		datosEnviar = new DatosBlackJack();
    		datosEnviar.setIdJugadores(idJugadores);
			datosEnviar.setValorManos(valorManos);
			datosEnviar.setCarta(carta);
			datosEnviar.setJugador(idJugadores[indexJugador]);
			
		
			
			datosEnviar.setApuestas(strArray(apuestas));	//*added apuestas
			
			
    		//determinar qué sucede con la carta dada en la mano del jugador y 
			//mandar mensaje a todos los jugadores
    		if(valorManos[indexJugador]>21) {
    			//jugador Voló
	    		//datosEnviar.setMensaje(idJugadores[indexJugador]+" tienes "+valorManos[indexJugador]+" volaste :(");	
	    		datosEnviar.setMensaje(idJugadoresSinApu[indexJugador]+" tienes "+valorManos[indexJugador]+" volaste :(");//*added apuestas	
	    		datosEnviar.setJugadorEstado("voló");
	    		
	    		//* added apuestas
	    		apuestas[indexJugador] = 0;
	    		datosEnviar.setApuestas(strArray(apuestas));
	    		//*
	    		
	    		jugadores[0].enviarMensajeCliente(datosEnviar);
	    		jugadores[1].enviarMensajeCliente(datosEnviar);
	    		jugadores[2].enviarMensajeCliente(datosEnviar);
	    		
	    		
	    		//notificar a todos que jugador sigue
	    		if(jugadorEnTurno==0) {
	        		
	        		datosEnviar = new DatosBlackJack();
		    		datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setJugador(idJugadores[1]);
					datosEnviar.setJugadorEstado("iniciar");
					//datosEnviar.setMensaje(idJugadores[1]+" te toca jugar y tienes " + valorManos[1]);
					datosEnviar.setMensaje(idJugadoresSinApu[1]+" te toca jugar y tienes " + valorManos[1]);//*added apuestas
					
					//* added apuestas
		    		apuestas[indexJugador] = 0;
		    		datosEnviar.setApuestas(strArray(apuestas));
		    		//*
					
					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);
					
					
					//levantar al jugador en espera de turno
					bloqueoJuego.lock();
		    		try {
						//esperarInicio.await();
						jugadores[0].setSuspendido(true);
						esperarTurno.signalAll();
						jugadorEnTurno++;
					}finally {
						bloqueoJuego.unlock();
					}
	        	}else if(jugadorEnTurno==1) {
	        		datosEnviar = new DatosBlackJack();
		    		datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setJugador(idJugadores[2]);
					datosEnviar.setJugadorEstado("iniciar");
					//datosEnviar.setMensaje(idJugadores[2]+" te toca jugar y tienes "+valorManos[2]);
					datosEnviar.setMensaje(idJugadoresSinApu[2]+" te toca jugar y tienes "+valorManos[2]);//*added apuestas
					//* added apuestas
		    		apuestas[indexJugador] = 0;
		    		datosEnviar.setApuestas(strArray(apuestas));
		    		//*
					
					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);
					
					
					//levantar al jugador en espera de turno
					bloqueoJuego.lock();
		    		try {
						//esperarInicio.await();
						jugadores[1].setSuspendido(true);
						esperarTurno.signalAll();
						jugadorEnTurno++;
					}finally {
						bloqueoJuego.unlock();
					}
	        		
	        		
	        		
	        		
	        		
	        		
	        		
	        		
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	}else {//era el jugador 2 entonces se debe iniciar el dealer
	        		//notificar a todos que le toca jugar al dealer
	        		datosEnviar = new DatosBlackJack();
		    		datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setJugador("dealer");
					datosEnviar.setJugadorEstado("iniciar");
					datosEnviar.setMensaje("Dealer se repartirá carta");

					//* added apuestas
		    		apuestas[indexJugador] = 0;
		    		datosEnviar.setApuestas(strArray(apuestas));
		    		//*
					
					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);
					
					iniciarDealer();
					
					//						REVISAR REVISAR REVISAR REVISAR REVISAR REVISAR REVISAR
					  
					  bloqueoJuego.lock();
		    		try {
						//esperarInicio.await();
						jugadores[2].setSuspendido(true);
						esperarTurno.signalAll();
						jugadorEnTurno++;
					}finally {
						bloqueoJuego.unlock();
					}
	        	}		
    		}else {//jugador no se pasa de 21 puede seguir jugando
    			datosEnviar.setCarta(carta);
    			datosEnviar.setJugador(idJugadores[indexJugador]);
    			//datosEnviar.setMensaje(idJugadores[indexJugador]+" ahora tienes "+valorManos[indexJugador]);
    			datosEnviar.setMensaje(idJugadoresSinApu[indexJugador]+" ahora tienes "+valorManos[indexJugador]);//*added apuestas
	    		datosEnviar.setJugadorEstado("sigue");
	    		
	    		jugadores[0].enviarMensajeCliente(datosEnviar);
	    		jugadores[1].enviarMensajeCliente(datosEnviar);
	    		jugadores[2].enviarMensajeCliente(datosEnviar);
    		}
    	}else {
    		//jugador en turno plantó
    		datosEnviar = new DatosBlackJack();
    		datosEnviar.setIdJugadores(idJugadores);
			datosEnviar.setValorManos(valorManos);
			datosEnviar.setJugador(idJugadores[indexJugador]);
			
    		//datosEnviar.setMensaje(idJugadores[indexJugador]+" se plantó");
    		datosEnviar.setMensaje(idJugadoresSinApu[indexJugador]+" se plantó");
    		datosEnviar.setJugadorEstado("plantó");
    		
    		jugadores[0].enviarMensajeCliente(datosEnviar);		    		
    		jugadores[1].enviarMensajeCliente(datosEnviar);
    		jugadores[2].enviarMensajeCliente(datosEnviar);
    		
    		//notificar a todos el jugador que sigue en turno
    		if(jugadorEnTurno==0) {
        		
        		datosEnviar = new DatosBlackJack();
	    		datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador(idJugadores[1]);
				datosEnviar.setJugadorEstado("iniciar");
				//datosEnviar.setMensaje(idJugadores[1]+" te toca jugar y tienes "+valorManos[1]);
				datosEnviar.setMensaje(idJugadoresSinApu[1]+" te toca jugar y tienes "+valorManos[1]);
				
				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				
				
				//levantar al jugador en espera de turno
				
				bloqueoJuego.lock();
	    		try {
					//esperarInicio.await();
					jugadores[indexJugador].setSuspendido(true);
					esperarTurno.signalAll();
					jugadorEnTurno++;
				}finally {
					bloqueoJuego.unlock();
				}
        	}else if(jugadorEnTurno==1) {
        		datosEnviar = new DatosBlackJack();
	    		datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador(idJugadores[2]);
				datosEnviar.setJugadorEstado("iniciar");
				//datosEnviar.setMensaje(idJugadores[2]+" te toca jugar y tienes "+valorManos[2]);
				datosEnviar.setMensaje(idJugadoresSinApu[2]+" te toca jugar y tienes "+valorManos[2]);//*added apuestas
				
				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				
				
				//levantar al jugador en espera de turno
				
				bloqueoJuego.lock();
	    		try {
					//esperarInicio.await();
					jugadores[indexJugador].setSuspendido(true);
					esperarTurno.signalAll();
					jugadorEnTurno++;
				}finally {
					bloqueoJuego.unlock();
				}
        		
        	} else {
        		//notificar a todos que le toca jugar al dealer
        		datosEnviar = new DatosBlackJack();
	    		datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador("dealer");
				datosEnviar.setJugadorEstado("iniciar");
				datosEnviar.setMensaje("Dealer se repartirá carta");
				
				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				
				
//levantar al jugador en espera de turno
				
				bloqueoJuego.lock();
	    		try {
					//esperarInicio.await();
					jugadores[indexJugador].setSuspendido(true);
					esperarTurno.signalAll();
					jugadorEnTurno++;
				}finally {
					bloqueoJuego.unlock();
				}
			
				iniciarDealer();
        	}	
    	}
   } 
    
    public void iniciarDealer() {
       //le toca turno al dealer.
    	Thread dealer = new Thread(this); //deales es una instancia de la clase ServidorBJ
    	dealer.start();
    }
    
    /*The Class Jugador. Clase interna que maneja el servidor para gestionar la comunicación
     * con cada cliente Jugador que se conecte
     */
    private class Jugador implements Runnable{
       
    	//varibles para gestionar la comunicación con el cliente (Jugador) conectado
        private Socket conexionCliente;
    	private ObjectOutputStream out;
    	private ObjectInputStream in;
    	private String entrada;
    	
    	//variables de control
    	private int indexJugador;
    	private boolean suspendido;
  
		public Jugador(Socket conexionCliente, int indexJugador) {
			this.conexionCliente = conexionCliente;
			this.indexJugador = indexJugador;
			suspendido = true;
			//crear los flujos de E/S
			try {
				out = new ObjectOutputStream(conexionCliente.getOutputStream());
				out.flush();
				in = new ObjectInputStream(conexionCliente.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}	
				
		private void setSuspendido(boolean suspendido) {
			this.suspendido = suspendido;
		}
	   
		@Override
		public void run() {
			// TODO Auto-generated method stub	
			//procesar los mensajes eviados por el cliente
			
			//ver cual jugador es 
			if(indexJugador==0) {
				//es jugador 1, debe ponerse en espera a la llegada de los otros jugadores
				
				try {
					//guarda el nombre del primer jugador
					idJugadores[0] = (String)in.readObject();
					mostrarMensaje("Hilo establecido con jugador (1) "+idJugadores[0]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mostrarMensaje("bloquea servidor para poner en espera de inicio al jugador 1");
				bloqueoJuego.lock(); //bloquea el servidor
				
				while(suspendido) {
					mostrarMensaje("Parando al Jugador 1 en espera de los otros jugadores...");
					try {
						esperarInicio.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						mostrarMensaje("Desbloquea Servidor luego de bloquear al jugador 1");
						bloqueoJuego.unlock();
					}
				}
				
				//ya se conectó el otro jugador, 
				//le manda al jugador 1 todos los datos para montar la sala de Juego
				//le toca el turno a jugador 1
				
				mostrarMensaje("manda al jugador 1 todos los datos para montar SalaJuego"); //REVISAR REVISAR REVISAR
				datosEnviar = new DatosBlackJack();
				datosEnviar.setManoDealer(manosJugadores.get(3));
				datosEnviar.setManoJugador1(manosJugadores.get(0));
				datosEnviar.setManoJugador2(manosJugadores.get(1));		
				datosEnviar.setManoJugador3(manosJugadores.get(2));
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				//datosEnviar.setMensaje("Inicias "+idJugadores[0]+" tienes "+valorManos[0]);
				datosEnviar.setMensaje("Inicias "+idJugadoresSinApu[0]+" tienes "+valorManos[0]);//*added apuestas
				enviarMensajeCliente(datosEnviar);
				jugadorEnTurno=0;
				
				
				
				
				
				
				
				
				
			}else if(indexJugador == 1) {
				
			
				 //Es jugador 2
				   //le manda al jugador 3 todos los datos para montar la sala de Juego
				   //jugador 2 debe esperar su turno
				try {
					idJugadores[1]=(String)in.readObject();
					
					//mostrarMensaje("Hilo jugador (2)"+idJugadores[1]);
					mostrarMensaje("Hilo jugador (2)"+idJugadoresSinApu[1]);//*added apuestas
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
				bloqueoJuego.lock();
				try {
					mostrarMensaje("Pone en espera de turno al jugador 3");
					esperarInicio.await();
					mostrarMensaje("Despierta de la espera de inicio del juego al jugador 1");
                 //
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					bloqueoJuego.unlock();
				}	
				
				
				mostrarMensaje("manda al jugador 3 el nombre del jugador 1 y del jugador 2");
				
				datosEnviar = new DatosBlackJack();
				datosEnviar.setManoDealer(manosJugadores.get(3));
				datosEnviar.setManoJugador1(manosJugadores.get(0));
				datosEnviar.setManoJugador2(manosJugadores.get(1));	
				datosEnviar.setManoJugador3(manosJugadores.get(2));		
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				//datosEnviar.setMensaje("Inicias "+idJugadores[0]+" tienes "+valorManos[0]);
				datosEnviar.setMensaje("Inicias "+idJugadoresSinApu[0]+" tienes "+valorManos[0]);//added apuestas
				
				enviarMensajeCliente(datosEnviar);
				
				//iniciarRondaJuego(); //despertar al jugador 1 para iniciar el juego
				mostrarMensaje("Bloquea al servidor para poner en espera de turno al jugador 3");
				
				
				
				
				
				
				
				
				
				
				
				
				
				
			}  else {
				   //Es jugador 3
				   //le manda al jugador 1 y 2 todos los datos para montar la sala de Juego
				   //jugador 3 debe esperar su turno
				try {
					idJugadores[2]=(String)in.readObject();
					idJugadoresSinApu = idsSinApuesta(idJugadores);//*added apuestas
					mostrarMensaje("Hilo jugador (3) " + idJugadores[2]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mostrarMensaje("manda al jugador 3 el nombre del jugador 1 y del jugador 2");
				
				datosEnviar = new DatosBlackJack();
				datosEnviar.setManoDealer(manosJugadores.get(3));
				datosEnviar.setManoJugador1(manosJugadores.get(0));
				datosEnviar.setManoJugador2(manosJugadores.get(1));	
				datosEnviar.setManoJugador3(manosJugadores.get(2));		
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				//datosEnviar.setMensaje("Inicias "+idJugadores[0]+" tienes "+valorManos[0]);
				datosEnviar.setMensaje("Inicias "+idJugadoresSinApu[0]+" tienes "+valorManos[0]);//*added apuestas
				
				
				
				
				//* added apuestas
	    		apuestasIniciales[0] = extraerApuesta(idJugadores[0]);
	    		apuestasIniciales[1] = extraerApuesta(idJugadores[1]);
	    		apuestasIniciales[2] = extraerApuesta(idJugadores[2]);
	    		
	    		apuestas = apuestasIniciales;
	    		
	    		datosEnviar.setApuestas(strArray(apuestas));
	    		//*
	    		
				
				enviarMensajeCliente(datosEnviar);
				
				
				//esperarInicio.signalAll();
				
				iniciarRondaJuego(); //despertar al jugador 1 para iniciar el juego
				mostrarMensaje("Bloquea al servidor para poner en espera de turno al jugador 3");
				bloqueoJuego.lock();
				try {
					mostrarMensaje("Pone en espera de turno al jugador 3");
					esperarTurno.await();
					mostrarMensaje("Despierta de la espera de inicio del juego al jugador 1");
                    //
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					bloqueoJuego.unlock();
				}
			}
			
			while(!seTerminoRonda(false)) {
				try {
					entrada = (String) in.readObject();
					analizarMensaje(entrada,indexJugador);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//controlar cuando se cierra un cliente
				}
			}
			//cerrar conexión
		}
		
		public void enviarMensajeCliente(Object mensaje) {
			try {  
				out.writeObject(mensaje);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
    }//fin inner class Jugador      

    //Jugador dealer emulado por el servidor
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mostrarMensaje("Incia el dealer ...");
        boolean pedir = true;
        
        String superMensaje;	//*added apuestas
        superMensaje = "";	//*added apuestas
        
        while(pedir) {
		  	Carta carta = mazo.getCarta();
			//adicionar la carta a la mano del dealer. Hasta ahora el dealer tenía una sola carta.
			manosJugadores.get(3).add(carta);
			calcularValorMano(carta, 3);
			
			mostrarMensaje("El dealer recibe " + carta.toString() + " suma "+ valorManos[3]);
			

    		datosEnviar = new DatosBlackJack();
			datosEnviar.setCarta(carta);
			datosEnviar.setJugador("dealer");
				
			if(valorManos[3]<=16) {
				datosEnviar.setJugadorEstado("sigue");
				datosEnviar.setMensaje("Dealer ahora tiene "+valorManos[3]);
				
				superMensaje += "Dealer ahora tiene "+valorManos[3] + "\n";//added apuestas
				
				mostrarMensaje("El dealer sigue jugando");
			}else {
				if(valorManos[3]>21) {
					datosEnviar.setJugadorEstado("voló");
					datosEnviar.setMensaje("Dealer ahora tiene "+valorManos[3]+" voló :(");
					superMensaje += "Dealer ahora tiene "+valorManos[3]+"  voló :(\n";
					pedir=false;
					mostrarMensaje("El dealer voló");
					
					//*added apuestas
					for(int p = 0; p < 3; p++) {
						if (manosJugadores.get(p).size() == 2 && valorManos[p] == 21) {
							apuestas[p] = 3 * (apuestas[p] / 2);
							mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " gana por BlackJack!");
							superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " gana por BlackJack!" + "\n";//added apuestas
						}else {
							if (apuestas[p] > 0) {
								apuestas[p] = 2 * apuestas[p];
								mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " recibe del dealer el valor de su apuesta y queda con: " + 
										String.valueOf(apuestas[p]));
								superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " recibe del dealer el valor de su apuesta y queda con: " + 
										String.valueOf(apuestas[p]) + "\n";//added apuestas
							}else {
								//Si el jugador ya había perdido su apuesta, no pasa nada.
								superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " ya había perdido su apuesta y queda con : " 
												+ String.valueOf(apuestas[p]) +"\n";//added apuestas
							}
						}
					}
					
					//*
					
				}else {
					datosEnviar.setJugadorEstado("plantó");
					datosEnviar.setMensaje("Dealer ahora tiene "+valorManos[3]+"  y se plantó"); 
					superMensaje += "Dealer ahora tiene "+valorManos[3]+"  y se plantó\n";
					pedir=false;
					mostrarMensaje("El dealer plantó");
					
					//*added apuestas
					for(int p = 0; p < 3; p++) {
						if (manosJugadores.get(p).size() == 2 && valorManos[p] == 21) {
							apuestas[p] += 3 * (apuestas[p] / 2);
							mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " gana por BlackJack! y queda con: " 
							+ String.valueOf(apuestas[p]) + "\n");
							superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " gana por BlackJack! y queda con: "
									+ String.valueOf(apuestas[p])  +"\n";
						}else if(valorManos[p] > valorManos[3]) {
							if (apuestas[p] > 0) {
								apuestas[p] = 2 * apuestas[p];
								mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " recibe del dealer el valor de su apuesta y queda con: " + 
										String.valueOf(apuestas[p]));
								superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " recibe del dealer el valor de su apuesta y queda con: " + 
										String.valueOf(apuestas[p]) + "\n";
							}else {
								//Si el jugador ya había perdido su apuesta, no pasa nada.
								mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " ya había perdido su apuesta y queda con: " + 
										String.valueOf(apuestas[p]));  
								superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " ya había perdido su apuesta y queda con: " + 
										String.valueOf(apuestas[p]) + "\n";
							}
						}else if(valorManos[p] == valorManos[3]) {
							mostrarMensaje("¡El jugador" + idJugadoresSinApu[p] + " conserva el valor de su apuesta y queda con: " + 
									String.valueOf(apuestas[p]));
							superMensaje += "¡El jugador" + idJugadoresSinApu[p] + " conserva el valor de su apuesta y queda con: " + 
									String.valueOf(apuestas[p]) + "\n";
						}else { // Jugador p pierde
							
							if(valorManos[p] == 0) {
								mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " ya había perdido su apuesta y queda con: " + 
										String.valueOf(apuestas[p]));
								superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " ya había perdido su apuesta y queda con: " + 
										String.valueOf(apuestas[p]) + "\n";
							}else {
								apuestas[p] = 0;
								mostrarMensaje("¡El jugador " + idJugadoresSinApu[p] + " pierde contra el dealer y queda con: " + 
										String.valueOf(apuestas[p]));
								superMensaje += "¡El jugador " + idJugadoresSinApu[p] + " pierde contra el dealer y queda con: " + 
										String.valueOf(apuestas[p]) + "\n";
							}
							
							
						}
					}
					//datosEnviar.setMensaje(superMensaje);
					//*
				}
			}

			
			datosEnviar.setMensaje(superMensaje);	//*added apuestas
			
			
			//envia la jugada a los otros jugadores
			datosEnviar.setCarta(carta);
			
			datosEnviar.setApuestas(strArray(apuestas));
			
			jugadores[0].enviarMensajeCliente(datosEnviar);
			jugadores[1].enviarMensajeCliente(datosEnviar);
			jugadores[2].enviarMensajeCliente(datosEnviar);
				
			
        }//fin while
        if (JOptionPane.showConfirmDialog(null, "¿Desea reiniciar?") == JOptionPane.YES_OPTION) {
        	restart();
        }
        
	}
    
}//Fin class ServidorBJ
