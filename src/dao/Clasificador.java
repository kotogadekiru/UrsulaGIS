package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.filter.function.Classifier;
import org.geotools.filter.function.JenksNaturalBreaksFunction;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import dao.cosecha.CosechaItem;
import gui.Messages;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import lombok.Data;
import utils.ProyectionConstants;
@Data
public class Clasificador {
	public static final String NUMERO_CLASES_CLASIFICACION = "NUMERO_CLASES_CLASIFICACION";
	private static final String CLASIFICADOR_JENKINS = "Jenkins";
	private static final String CLASIFICADOR_DESVIOSTANDAR = "Desvio Standar";
	public static final String TIPO_CLASIFICADOR = "CLASIFICADOR";

	public static final  String[] clasficicadores = {CLASIFICADOR_DESVIOSTANDAR,CLASIFICADOR_JENKINS};
	public static Color[] colors = {
			//Color.rgb(158,1,66),//0
			//Color.rgb(213,62,79),//1
			Color. rgb(244,109,67),//2
			Color. rgb(253,174,97),//3
			Color. rgb(254,224,139),//4
			Color. rgb(255,255,191),//5
			Color. rgb(230,245,152)	,//6
			Color. rgb(171,221,164),//7
			Color.rgb(102,194,165),//8
			Color.rgb(50,136,189),//9
			Color.DARKBLUE};//10
	//		Color.rgb(94,79,162)
	//		};
	private  Double[] histograma=null;// es static para poder hacer constructHistograma static para usarlo en el grafico de Histograma
	private  Classifier clasifier=null;
	public StringProperty tipoClasificadorProperty;
	public IntegerProperty clasesClasificadorProperty;
	private boolean initialized=false;

	public Clasificador(){
		tipoClasificadorProperty = new SimpleStringProperty();		

	}

	public String getCategoryNameFor(int index) {		
		String rangoIni = null;
		DecimalFormat df = new DecimalFormat("0.00");
		df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("EN")));
		df.setGroupingSize(3);
		df.setGroupingUsed(true);
		if(histograma != null){
		//	Double delta = histograma[1]-histograma[0];

			if(index == 0){
				rangoIni = "-inf ~ "+ df.format(histograma[index]);
			}else if(index<histograma.length){
				rangoIni = df.format(histograma[index-1])+" ~ "+ df.format(histograma[index]);
			}else {
				rangoIni = df.format(histograma[index-1])+" ~ +inf";//+(histograma[index]+delta);
			}
			return rangoIni;
		} else if(clasifier != null){			
			rangoIni = clasifier.getTitle(index);	
			String [] partesIni = rangoIni.split("\\.\\.");			
			return df.format(new Double(partesIni[0]))+" ~ "+df.format(new Double(partesIni[1]));// +"-"+histograma[j+1];
		}
		return "error";
//		if(rangoIni!=null){
//		
//		} else{
//			return "error";
//		}
		//		System.err.println("Error no hay un clasificador seleccionado");
		//		return label;
	}

	public  Integer getCategoryFor(Double rinde) {				

		if(histograma != null){
			int absColor = getColorByHistogram(rinde, histograma);
			return absColor;
			//			System.out.println("obteniendo la clase para el ");
			//			return absColor*(colors.length-1)/clasesClasificadorProperty.get();

		} else if(clasifier != null){
			int absColor = getColorByJenks(rinde);
			return absColor;
			//	return absColor*(colors.length-1)/clasesClasificadorProperty.get();
		}
		//System.err.println("Error no hay un clasificador seleccionado");
		return 0;
		//return getColorByHue(rinde, rindeMin, rindeMax, porcent);
	}


	public static int getColorByHistogram(Double rinde, Double[] histo) {
		int colorIndex = histo.length-1;
		try {
			BigDecimal bd = new BigDecimal(rinde);//java.lang.NumberFormatException: Infinite or NaN
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			rinde = bd.doubleValue();
			for (int i = histo.length-1; i > -1 ; i--) {
				double histoMax = histo[i];
				if (rinde <= histoMax) {
					colorIndex = i;
				}
			}

			//	 System.out.println("Histograma color Index for rinde "+rinde+" is "+colorIndex);

			return colorIndex;
		} catch (Exception e) {
		//	System.err.println("getColorsByHistogram "+rinde);
			//e.printStackTrace();
			return 0;
		}
	}

	public  Classifier constructJenksClasifier(SimpleFeatureCollection collection,String amountColumn){
		//JenksFunctionTest test = new JenksFunctionTest("jenksTest");
		histograma = null;
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

		//Literal classes = ff.literal(colors.length);
		Literal classes = ff.literal(this.getNumClasses());
		PropertyName expr = ff.property(amountColumn);
		JenksNaturalBreaksFunction func = (JenksNaturalBreaksFunction) ff.function("Jenks", expr,
				classes);

		
		
		//TODO construir una colleccion equivalente pero donde cada feature tenga la misma superficie
		//10m^2 para que tengan el mismo peso relativo
		if(collection.size()>0){
			System.out.println("evaluando la colleccion para poder hacer jenkins");
			clasifier = (Classifier) func.evaluate(collection);//XXX esto demora unos segundos!
		} else{
			System.out.println("no se pudo evaluar jenkins porque la coleccion de datos es de tamanio cero");
		}
		if(clasifier == null){
			System.out.println("No se pudo evaluar la colleccion de features con el metodo de Jenkins");

		}
		//  int clase =   clasifier.classify(arg0)
		return clasifier;
	}

	private  int getColorByJenks(Double double1) {
		try{
			int colorIndex = clasifier.classify(double1);

			if(colorIndex<0||colorIndex>colors.length){
				//System.out.println("el color de jenks es: "+colorIndex+" para el rinde "+double1);//rinde es 0.0
				colorIndex=0;
			}
			return colorIndex;
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 
	 * @param elementos Lista de FeatureContainer
	 * @return 
	 */
	public  Double[] constructHistogram(List<? extends LaborItem> elementosItem){

		//1 ordeno los elementos de menor a mayor
		//2 bsuco el i*size/12 elemento y anoto si amount en la posicion i del vector de rangos

		//		List<Dao> elementos = new LinkedList<Dao>(elementosItem);
		//		elementos.sort((e1, e2) -> e1.getAmount().compareTo(e2.getAmount()));//sort ascending



		Double average = new Double(0);
		Double sup= new Double(0);
		Double amount= new Double(0);
		for(LaborItem dao: elementosItem){
			Double area = dao.getGeometry().getArea()*ProyectionConstants.A_HAS();
			sup +=area;
			amount+=dao.getAmount()*area;		
		}
		average=sup>0?amount/(sup):0.0;

		//		average = elementosItem
		//				.stream().mapToDouble( FeatureContainer::getAmount)
		//				.average().getAsDouble();//no such value???

		Double desvioEstandar =new Double(0);
		if(elementosItem.size()>0){

			double desvios = new Double(0);
			for(LaborItem dao: elementosItem){
				Double area = dao.getGeometry().getArea()*ProyectionConstants.A_HAS();
				desvios += area*Math.abs(dao.getAmount()-average);
				//desvios += Math.abs(dao.getAmount()-average);
			}
			desvioEstandar= desvios/sup;
			//desvioEstandar= desvios/(elementosItem.size());
		}

		//	System.out.println("termine de ordenar los elementos en constructHistogram");
		int numClases = getNumClasses()-1;//esto es porque el histograma se extiende hacia el infinito por lo que gana una clase
		histograma=new Double[numClases];

		int desviosEstandar =numClases;// 8;//es la cantidad de desvios que voy a dibujar
		Double deltaForColour =(desviosEstandar*desvioEstandar)/numClases;

		for(int i = 0;i<numClases;i++){	
			histograma[i]=(average-(desviosEstandar/2)*desvioEstandar)+deltaForColour*(i+1);
		}

		this.initialized=true;
		return histograma;
	}

	public Color getColorFor(double amount) {
		int absCat = getCategoryFor(amount);//entre 0 y numClases-1
		return getColorForCategoria(absCat);
//		int length =colors.length-1;
//		int clases =getNumClasses()-1;
//		int colorIndex = absCat*(length/clases);
//		//	System.out.println(absCat+"*"+length+"/"+clases+" = "+colorIndex+" colorIndex");
//		if(colorIndex>length){
//			colorIndex=length;
//		}
//		return colors[colorIndex];
	}

	public Color getColorForCategoria(Integer absCat) {
		//int absCat = getCategoryFor(amount);//entre 0 y numClases-1
		int length =colors.length-1;
		int clases =getNumClasses()-1;
		int colorIndex = absCat*(length/clases);
		//	System.out.println(absCat+"*"+length+"/"+clases+" = "+colorIndex+" colorIndex");
		if(colorIndex>length){
			colorIndex=length;
		}
		return colors[colorIndex];
	}
	
	public Color getColorFor(LaborItem dao) {	
		return getColorFor(dao.getAmount());

	}

	public int getNumClasses() {
		int numClases = clasesClasificadorProperty.intValue();
		//return 3;
		if(numClases>colors.length|| numClases<1){
			System.err.println("la configuracion default de "+NUMERO_CLASES_CLASIFICACION+" no puede ser mayor a "+(colors.length));
			numClases=colors.length;
		}
		return numClases;
	}
	
	public boolean isInitialized(){return initialized;}

	public void constructClasificador(String nombreClasif, Labor<?> labor) {
		System.out.println("constructClasificador "+nombreClasif);
		if (Clasificador.CLASIFICADOR_JENKINS.equalsIgnoreCase(nombreClasif)) {
			System.out.println("construyendo clasificador jenkins "+labor.colAmount.get());
			
			//*** nuevo codigo para tomar en cuenta el area de los poligonos
//			SimpleFeatureCollection areaInvariantCol = new DefaultFeatureCollection("internal",labor.getType());
//			
//			FeatureReader<SimpleFeatureType, SimpleFeature> reader=null;
//			try {
//				reader = labor.getInCollection().reader();
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//			
//			SimpleFeatureBuilder fb = labor.featureBuilder;
//			
//			int id=0;
//			final double minArea=10;
//			while (ProcessMapTask.readerHasNext(reader)) {
//				SimpleFeature feature=null;
//				try {
//					feature = reader.next();
//					
//					Double area = ProyectionConstants.getHasFeature(feature)							
//							*ProyectionConstants.METROS2_POR_HA;
//					System.out.println("multiplicando el feature con area original "+ area+" id "+id);//16.11?
//					while(area > 0) {
//						areaInvariantCol.add(fb.buildFeature("\\."+id, feature.getAttributes().toArray()));
//						area=area-minArea;
//						id++;
//					}
//					
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}				
//			}
//			System.out.println("haciendo jenkins con la nueva collection de "+id+" features");
//			this.constructJenksClasifier(areaInvariantCol,labor.colAmount.get());
//			areaInvariantCol.clear();
			//** fin del nuevo codigo para tomar en cuenta el area de los poligonos
			
			this.constructJenksClasifier(labor.outCollection,labor.colAmount.get());
		
		} else {//if(Clasificador.CLASIFICADOR_DESVIOSTANDAR.equalsIgnoreCase(nombreClasif)) {
			System.out.println("no hay jenks Classifier falling back to histograma");
			List<LaborItem> items = new ArrayList<LaborItem>();

			SimpleFeatureIterator ocReader = labor.outCollection.features();
			while (ocReader.hasNext()) {
				items.add(labor.constructFeatureContainerStandar(ocReader.next(),false));
			}
			ocReader.close();
			this.constructHistogram(items);
		}
		
		
		DefaultFeatureCollection newOutcollection =  new DefaultFeatureCollection(Messages.getString("ProcessHarvestMapTask.9"),labor.getType());
		
		SimpleFeatureIterator it = labor.outCollection.features();
		while(it.hasNext()){
			SimpleFeature fIn = it.next();

			LaborItem li=labor.constructFeatureContainerStandar(fIn,false);
			li.setCategoria(this.getCategoryFor(li.getAmount()));
			SimpleFeature f = li.getFeature(labor.featureBuilder);
			boolean res = newOutcollection.add(f);
	
		}
		labor.setOutCollection(newOutcollection);
		
	}

	

	//	public Clasificador clone(){
	//		Clasificador cn = new Clasificador();
	//		cn.setClasesClasificadorProperty(new SimpleIntegerProperty(this.getClasesClasificadorProperty().get()));
	//		cn.getTipoClasificadorProperty().set(this.getTipoClasificadorProperty().get());
	//		//XXX si el tipo de clasificador es jenkins hay que volver a constriur el clasificador
	//		cn.setHistograma(this.getHistograma().clone());
	//		cn.setInitialized(this.isInitialized());
	//		return cn;
	//	}

	//	/**
	//	 * Metodo que busca los limites de las alturas despues hay que buscar los elementos que estan dentro de un entorno y agregarlos a una lista para dibujarlos
	//	 * @param elementos Lista de Dao ordenados por Elevacion de menor a mayor
	//	 * @return 
	//	 */
	//	public static Double[] constructHeightstogram(List<? extends CosechaItem> elementosItem){
	//		double average = elementosItem
	//				.stream().mapToDouble( CosechaItem::getElevacion)
	//				.average().getAsDouble();
	//		double desvios = new Double(0);
	//		for(CosechaItem dao: elementosItem){
	//			desvios += Math.abs(dao.getElevacion()-average);
	//		}
	//		double desvioEstandar= desvios/elementosItem.size();
	//		heightstogram=new Double[colors.length];
	//
	//		int desviosEstandar = 8;
	//		Double deltaForColour =(desviosEstandar*desvioEstandar)/colors.length;
	//
	//		for(int i = 0;i<colors.length;i++){	
	//			heightstogram[i]=(average-(desviosEstandar/2)*desvioEstandar)+deltaForColour*(i+1);
	//		}
	//		return heightstogram;
	//	}
}
