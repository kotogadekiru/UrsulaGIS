/*
package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.identity.FeatureId;

import tasks.HarvestFiltersConfig;

import com.vividsolutions.jts.geom.Geometry;

public class Cosecha extends Dao{	
	private static final int KG_POR_TN = 1000;
	
	private static final String COLUMNA_VELOCIDAD = "Velocidad";
	public static final String COLUMNA_RENDIMIENTO = "Rendimiento";
	private static final String COLUMNA_ANCHO = "Ancho";
	private static final String COLUMNA_CURSO = "Curso(deg)";
	private static final String COLUMNA_DISTANCIA = "Distancia";
	private static final String COLUMNA_PASADA = "Num__de_pa";
	//private static final String COLUMNA_ID = "Id_obj_";

	private static final String COLUMNA_PRECIO = "precio_grano";

	private static final String COLUMNA_IMPORTE_HA = "importe_ha";
	
	private static Map<String, String> columnsMap= new HashMap<String, String>();

	
	private static Double correccionRinde = new Double(100);
	
	Double precioTnGrano;
	


	public Cosecha(SimpleFeature harvestFeature, Double precioGrano) {
		super(harvestFeature);
	
		//this.geometry = (Geometry) harvestFeature.getDefaultGeometry();
		
		double toMetros=HarvestFiltersConfig.getInstance().getMetrosPorUnidadDistancia();
	
			
		Double rindeDouble =  super.getDoubleFromObj(harvestFeature.getAttribute(getColumn( COLUMNA_RENDIMIENTO)));
		
		if(rindeDouble>100){//esta en kilogramos
			rindeTnHa = rindeDouble *(correccionRinde/100)/KG_POR_TN ;
		} else if(rindeDouble <1){ //esta en decenas de toneladas?
		
				rindeTnHa = rindeDouble *(correccionRinde/100);// *4.28;
		}else{
			rindeTnHa = rindeDouble *(correccionRinde/100) ;
		}
	

		velocidad = super.getDoubleFromObj(harvestFeature.getAttribute(getColumn(COLUMNA_VELOCIDAD)))*toMetros;
		this.precioTnGrano = precioGrano;
		this.importeHa = rindeTnHa * this.precioTnGrano;
	}



	public Double getDistancia() {
		return distancia;
	}

	public void setDistancia(Double distancia) {
		this.distancia = distancia;
	}

	public Double getRumbo() {
		return rumbo;
	}

	public void setRumbo(Double rumbo) {
		this.rumbo = rumbo;
	}

	public Double getAncho() {
		return ancho;
	}
	
	public Double getPasada() {
		return pasada;
	}

	public Double getId() {
		return id;
	}

	public void setAncho(Double ancho) {
		this.ancho = ancho;
	}

	public Double getRindeTnHa() {
		return rindeTnHa;
	}

	public void setRindeTnHa(Double rindeTnHa) {
		this.rindeTnHa = rindeTnHa;
	}

	public Double getVelocidad() {
		return velocidad;
	}

	public void setVelocidad(Double velocidad) {
		this.velocidad = velocidad;
	}

	public Double getPrecioTnGrano() {
		return precioTnGrano;
	}

	public void setPrecioTnGrano(Double precioTnGrano) {
		this.precioTnGrano = precioTnGrano;
	}

	public Double getImporteHa() {
		this.importeHa = rindeTnHa * this.precioTnGrano;
		return importeHa;
	}

	public void setImporteHa(Double importeHa) {
		this.importeHa = importeHa;
	}
	
	public static List<String> getRequieredColumns() {
		List<String> requiredColumns = new ArrayList<String>();
		requiredColumns.add(COLUMNA_VELOCIDAD);		
		requiredColumns.add(COLUMNA_RENDIMIENTO);	
		requiredColumns.add(COLUMNA_ANCHO);	
		requiredColumns.add(COLUMNA_CURSO);	
		requiredColumns.add(COLUMNA_DISTANCIA);	
		requiredColumns.add(COLUMNA_PASADA);	
	//	requiredColumns.add(COLUMNA_ID);	
		return requiredColumns;
	}

	@Override
	public Double getAmount() {
		return getRindeTnHa();
	}



	@Override
	protected Map<String, String> getColumnsMap() {
		return Cosecha.columnsMap;
	}

	public static String getColumnName(String internalName){
		return Cosecha.columnsMap.get(internalName);
	}


	public static void setColumnsMap(Map<String, String> columns) {
		Cosecha.columnsMap.clear();
		Cosecha.columnsMap.putAll(columns);		
		//TODO guardar las columnas en el archivo de configuracion
		columns.forEach(new BiConsumer<String, String>(){
			@Override
			public void accept(String key, String value) {
				Configuracion.getInstance().setProperty(key, value);				
			}
			
		});
		
	}



	public static void setCorreccionRinde(Double correccionRinde1) {
		//System.out.println("nuevo correccion rinde es "+correccionRinde1);
		correccionRinde  = correccionRinde1;
		
	}
	public static SimpleFeatureType getType(){
		SimpleFeatureType type=null;
		try {
			*/
/*
			 geom tiene que ser Point, Line o Polygon. no puede ser Geometry porque podria ser cualquiera y solo permite un tipo por archivo
			 los nombre de las columnas no pueden ser de mas de 10 char
			  *//*

			
			type = DataUtilities.createType("Cosecha",
					"*geom:Polygon,"
					+ Cosecha.COLUMNA_DISTANCIA+":Double,"
					+ Cosecha.COLUMNA_CURSO+":Double,"
					+ Cosecha.COLUMNA_ANCHO+":Double,"
					+ Cosecha.COLUMNA_RENDIMIENTO+":Double,"
					+ Cosecha.COLUMNA_VELOCIDAD+":Double,"
					+ Cosecha.COLUMNA_PRECIO+":Double,"
					+Cosecha.COLUMNA_IMPORTE_HA+":Double"
			);
		} catch (SchemaException e) {
			
			e.printStackTrace();
		}
		return type;
	}

@Override
	public SimpleFeature getFeature(SimpleFeatureBuilder featureBuilder) {
	featureBuilder.add(super.getGeometry());
	featureBuilder.add(getDistancia());
	featureBuilder.add(getRumbo());
	featureBuilder.add(getAncho());
	featureBuilder.add(getRindeTnHa());
	featureBuilder.add(getVelocidad());		
	featureBuilder.add(getPrecioTnGrano());
	featureBuilder.add(getImporteHa());		
			
	SimpleFeature feature = featureBuilder.buildFeature(null);
	return feature;
	}


}
*/
