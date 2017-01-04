package dao;

import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.util.Arrays;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class FeatureContainer implements Comparable<FeatureContainer>{
	protected Double id=new Double(0);
	protected Geometry geometry;
	

	protected Double distancia =new Double(0);
	protected Double rumbo=new Double(0);
	protected Double ancho=new Double(0);
	protected Double elevacion=new Double(0);
	protected Integer categoria=new Integer(0);
	
	protected Double areaSinSup= new Double(0);


	public FeatureContainer(SimpleFeature feature) {
		this.geometry = (Geometry) feature.getDefaultGeometry();
	}

	public FeatureContainer() {

	}

	public void setGeometry(Geometry geom) {
		if(geom instanceof Point){
			Point p = (Point) geom;
			Coordinate c=p.getCoordinate();
			if(c.x==0 &&c.y==0){
				System.out.println("seteando una geometria POINT 0,0 "+geom);
				return;
			}
		}
		this.geometry = geom;

	}

	public  Geometry getGeometry(){
		return geometry;
	}


	public Integer getCategoria() {
		return categoria;
	}



	public void setCategoria(Integer categoria) {
		this.categoria = categoria;
	}

	/**
	 * @return the distancia
	 */
	public Double getDistancia() {
		return distancia;
	}

	/**
	 * @param distancia the distancia to set
	 */
	public void setDistancia(Double distancia) {
		this.distancia = distancia;
	}

	/**
	 * @return the rumbo
	 */
	public Double getRumbo() {
		return rumbo;
	}

	/**
	 * @param rumbo the rumbo to set
	 */
	public void setRumbo(Double rumbo) {
		this.rumbo = rumbo;
	}

	/**
	 * @return the ancho
	 */
	public Double getAncho() {
		return ancho;
	}

	/**
	 * @param ancho the ancho to set
	 */
	public void setAncho(Double ancho) {
		this.ancho = ancho;
	}

	/**
	 * @return the elevacion
	 */
	public Double getElevacion() {
		return elevacion;
	}

	/**
	 * @param elevacion the elevacion to set
	 */
	public void setElevacion(Double elevacion) {
		this.elevacion = elevacion;
	}

	public int compareTo(FeatureContainer dao){
		return getAmount().compareTo(dao.getAmount());

	}

	public abstract Double getAmount();		
	
	public abstract Double getImporteHa();		

	public Double getId() {
		return id;
	}

	public void setId(Double _id) {
		id=_id;
	}
	//protected abstract Map<String, String> getColumnsMap();


	public static String getID(SimpleFeature harvestFeature) {
		String identifier = harvestFeature.getIdentifier().getID();
		String[] split = identifier.split("\\.");
		if (split.length > 1) {
			return split[split.length - 1];
		}

		return "0.0";
	}

	public static Double getDoubleFromObj(Object o){
		Double d = new Double(0); 
		if(o instanceof Double){
			d = (Double) o;
		} else  if(o instanceof Integer){
			d = new Double((Integer) o);
		} else  if(o instanceof Long){
			d = new Double((Long) o);
		} else if(o instanceof String){
			StringConverter<Number> converter = new NumberStringConverter();

			try{
				d=converter.fromString((String) o).doubleValue();
				//	d = new Double((String) o);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			System.err.println("no se pudo leer la cantidad de " +o);//no se pudo leer la cantidad de L3:CARG0003

		}
		return d;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof FeatureContainer){
			return compareTo((FeatureContainer)o) == 0;
		} else{
			return false;
		}
	}

	@Override public int hashCode() {
		int result = this.getAmount().intValue();

		return result;
	}



	public Double getAreaSinSup() {
		return areaSinSup;
	}

	public void setAreaSinSup(Double areaSinSup) {
		this.areaSinSup = areaSinSup;
	}



	//abstract public SimpleFeature getFeature(SimpleFeatureBuilder featureBuilder);

	public SimpleFeature getFeature(SimpleFeatureBuilder featureBuilder) {
		Object[] basicElements = new Object[]{
				this.getGeometry(),
				distancia,
				rumbo,
				ancho,
				elevacion,
				getCategoria()};
	
		Object[] specialElements= getSpecialElementsArray();
		Object[] completeElements = new Object[basicElements.length+specialElements.length];
		for(int i =0;i<basicElements.length;i++){
			completeElements[i]=basicElements[i];
		}
		
		for(int i =0;i<specialElements.length;i++){
			completeElements[i+basicElements.length]=
					specialElements[i];
		}
		
	
	featureBuilder.addAll(completeElements);
		
		
	//System.out.println("construyendo el simplefeature para el id:"+this.getId());//construuendo el simplefeature para el id:0.0
	SimpleFeature feature = featureBuilder.buildFeature("\\."+this.getId().intValue());
	
	return feature;
	}
	
//	public SimpleFeature getFeatureAsPoint(SimpleFeatureBuilder featureBuilder) { 
//		this.geometry=this.geometry.getCentroid();
//		return getFeature(featureBuilder);
//	}
	
	/**
	 * devuelve un array con los elementos particulares de la subclase
	 * @return
	 */
	public abstract Object[] getSpecialElementsArray();

}