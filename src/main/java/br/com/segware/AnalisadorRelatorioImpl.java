package br.com.segware;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalisadorRelatorioImpl implements IAnalisadorRelatorio {

	public String caminho;
	public BufferedReader br = null;
	public BufferedReader brAux = null;
	public String linha = "";
	public String split = ",";
	
	public AnalisadorRelatorioImpl() throws FileNotFoundException{
		caminho = "src/test/java/br/com/segware/relatorio.csv";
		br = new BufferedReader(new FileReader(caminho));
	}

	public Map<String, Integer> getTotalEventosCliente() {
		
		Map<String, Integer> totalEventosCliente = new HashMap<String, Integer>();
		
		try {		
			while((linha = br.readLine()) != null){
				String[] vetorAux = linha.split(split);
				if(totalEventosCliente.containsKey(vetorAux[1])){
					totalEventosCliente.put(vetorAux[1], totalEventosCliente.get(vetorAux[1])+1);
				}
				else{
					totalEventosCliente.put(vetorAux[1], 1);
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return totalEventosCliente;
	}

	public Map<String, Long> getTempoMedioAtendimentoAtendente() {
		Map<String, Long> tempoMedioAtendimentoAtendente = new HashMap<String, Long>();
		Map<String, Integer> quantidadeDeAtentimentosAtendente = new HashMap<String, Integer>();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		try {		
			while((linha = br.readLine()) != null){
				String[] vetorAux = linha.split(split);
				Date inicio = (Date) formatter.parse(vetorAux[4]);
				Date fim = (Date) formatter.parse(vetorAux[5]);
				if(tempoMedioAtendimentoAtendente.containsKey(vetorAux[6])){
					tempoMedioAtendimentoAtendente.put(vetorAux[6], (Long)tempoMedioAtendimentoAtendente.get(vetorAux[6])+((fim.getTime() - inicio.getTime())/1000));
					quantidadeDeAtentimentosAtendente.put(vetorAux[6], quantidadeDeAtentimentosAtendente.get(vetorAux[6])+1);
				}
				else{
					tempoMedioAtendimentoAtendente.put(vetorAux[6], (Long)((fim.getTime() - inicio.getTime())/1000));
					quantidadeDeAtentimentosAtendente.put(vetorAux[6], 1);
				}	
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		Set<String> atendentes = tempoMedioAtendimentoAtendente.keySet();
		
		for(String atendente : atendentes){
			tempoMedioAtendimentoAtendente.put(atendente, tempoMedioAtendimentoAtendente.get(atendente)/quantidadeDeAtentimentosAtendente.get(atendente));
		}
		
		return tempoMedioAtendimentoAtendente;
	}

	public List<Tipo> getTiposOrdenadosNumerosEventosDecrescente() {
		List<Tipo> tiposOrdenadosNumerosEventosDecrescente = new ArrayList<Tipo>();
		Map<String, Integer> totalEventosPorTipo = new HashMap<String, Integer>();

		try {		
			while((linha = br.readLine()) != null){
				String[] vetorAux = linha.split(split);
				if(totalEventosPorTipo.containsKey(vetorAux[3])){
					totalEventosPorTipo.put(vetorAux[3], totalEventosPorTipo.get(vetorAux[3])+1);
				}
				else{
					totalEventosPorTipo.put(vetorAux[3], 1);
				}			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List list = new LinkedList<>(totalEventosPorTipo.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
                                       .compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			switch ((String) entry.getKey()){
				case "ALARME":
					tiposOrdenadosNumerosEventosDecrescente.add(Tipo.ALARME);
					break;
				case "DESARME":
					tiposOrdenadosNumerosEventosDecrescente.add(Tipo.DESARME);
					break;
				case "TESTE":
					tiposOrdenadosNumerosEventosDecrescente.add(Tipo.TESTE);
					break;
				case "ARME":
					tiposOrdenadosNumerosEventosDecrescente.add(Tipo.ARME);
					break;
			}
		}
		
		return tiposOrdenadosNumerosEventosDecrescente;
	}

	/* Achei a descrição deste método um pouco difícil de se entender,
	 * primeiro pensei que deveria verificar os alarmes em geral sem restrições
	 * de cliente, pois a descrição do mesmo não falava nada sobre cliente.
	 * Pensando assim todos os eventos de desarmes entrariam nesta lista, o que
	 * não bate com o teste implementado... Após quebrar a cabeça um pouco
	 * pensando sobre as possíveis restrições deste método, acabei considerando
	 * que deveria verificar separando por clientes, desta forma acabei
	 * conseguindo fazer com que o teste obtivesse sucesso */
	
	public List<Integer> getCodigoSequencialEventosDesarmeAposAlarme() {
		List<Integer> codigoSequencialEventosDesarmeAposAlarme = new ArrayList<Integer>();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		
		try {
			while((linha = br.readLine()) != null){
				String[] vetorAux = linha.split(split);
				String linhaAux;
				brAux = new BufferedReader(new FileReader(caminho));
				while((linhaAux = brAux.readLine()) != null){
					String[] linhaVetorAux = linhaAux.split(split);
					if(vetorAux[1].equals(linhaVetorAux[1]) && vetorAux[3].equals("ALARME") && linhaVetorAux[3].equals("DESARME")){
						Date horaDesarme = (Date) formatter.parse(linhaVetorAux[4]);
						Date horaAlarme = (Date) formatter.parse(vetorAux[4]);
						Long horaDesarmeSegundos = horaDesarme.getTime()/1000;
						Long horaAlarmeSegundos = horaAlarme.getTime()/1000;
						if(horaDesarmeSegundos - horaAlarmeSegundos <= 300 && horaDesarmeSegundos - horaAlarmeSegundos >= 0 && !codigoSequencialEventosDesarmeAposAlarme.contains(linhaVetorAux[0])){
							codigoSequencialEventosDesarmeAposAlarme.add(Integer.parseInt(linhaVetorAux[0]));
						}
					}
				}
				brAux.close();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		return codigoSequencialEventosDesarmeAposAlarme;
	}

}
