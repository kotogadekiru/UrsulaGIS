package tasks.importar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import dao.LaborItem;
import dao.cosecha.CosechaItem;
import dao.fertilizacion.FertilizacionItem;
import dao.fertilizacion.FertilizacionLabor;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gui.Messages;
import javafx.scene.Group;
import javafx.scene.shape.Path;
import tasks.ProcessMapTask;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import utils.ProyectionConstants;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *   Cuando el ingreso marginal es igual al costo unitario el beneficio ($/Ha) de agregar N es m�ximo
 * @author tomas
 *
 */
public class ProcessFertMapTask extends ProcessMapTask<FertilizacionItem,FertilizacionLabor> {

	public ProcessFertMapTask(FertilizacionLabor labor) {
		super(labor);
	}

	public void doProcess() throws IOException {
		//	System.out.println("doProcess(); "+System.currentTimeMillis());

		FeatureReader<SimpleFeatureType, SimpleFeature> reader =null;
		//	CoordinateReferenceSystem storeCRS =null;
		if(labor.getInStore()!=null){
			if(labor.outCollection!=null)labor.outCollection.clear();
			reader = labor.getInStore().getFeatureReader();
			//		 storeCRS = labor.getInStore().getSchema().getCoordinateReferenceSystem();
			//convierto los features en cosechas
			featureCount=labor.getInStore().getFeatureSource().getFeatures().size();
		} else{//XXX cuando es una grilla los datos estan en outstore y instore es null
			reader = labor.outCollection.reader();
			//	 storeCRS = labor.outCollection.getSchema().getCoordinateReferenceSystem();
			//convierto los features en cosechas
			featureCount=labor.outCollection.size();
		}
		
		System.out.println(Messages.getString("ProcessFertMapTask.0")+DataUtilities.spec(reader.getFeatureType())); //$NON-NLS-1$

		int divisor = 1;

		while (reader.hasNext()) {
			SimpleFeature simpleFeature = reader.next();
			FertilizacionItem ci = labor.constructFeatureContainer(simpleFeature);

			featureNumber++;

			updateProgress(featureNumber/divisor, featureCount);
			Object geometry = ci.getGeometry();

			/**
			 * si la geometria es un point procedo a poligonizarla
			 */
			if (geometry instanceof Point) {
				//				Point longLatPoint = (Point) geometry;
				//
				//			
				//				if(	lastX!=null && labor.getConfiguracion().correccionDistanciaEnabled()){
				//					
				//					double aMetros=1;// 1/ProyectionConstants.metersToLongLat;
				//				//	BigDecimal x = new BigDecimal();
				//					double deltaY = longLatPoint.getY()*aMetros-lastX.getY()*aMetros;
				//					double deltaX = longLatPoint.getX()*aMetros-lastX.getX()*aMetros;
				//					if(deltaY==0.0 && deltaX ==0.0|| lastX.equals(longLatPoint)){
				//						puntosEliminados++;
				//					//	System.out.println("salteando el punto "+longLatPoint+" porque tiene la misma posicion que el punto anterior "+lastX);
				//						continue;//ignorar este punto
				//					}
				//				
				//					double tan = deltaY/deltaX;//+Math.PI/2;
				//					rumbo = Math.atan(tan);
				//					rumbo = Math.toDegrees(rumbo);//como esto me da entre -90 y 90 le sumo 90 para que me de entre 0 180
				//					rumbo = 90-rumbo;
				//
				//					/**
				//					 * 
				//					 * deltaX=0.0 ;deltaY=0.0
				//					 *	rumbo1=NaN
				//					 *	rumbo0=310.0
				//					 */
				//				
				//					if(rumbo.isNaN()){//como el avance en x es cero quiere decir que esta yerndo al sur o al norte
				//						if(deltaY>0){
				//							rumbo = 0.0;
				//						}else{
				//							rumbo=180.0;
				//						}
				//					}
				//
				//					if(deltaX<0){//si el rumbo estaba en el cuadrante 3 o 4 sumo 180 para volverlo a ese cuadrante
				//						rumbo = rumbo+180;
				//					}
				//					ci.setRumbo(rumbo);
				//
				//				}
				//			
				//				lastX=longLatPoint;
				//				Double alfa  = Math.toRadians(rumbo) + Math.PI / 2;
				//
				//				// convierto el ancho y la distancia a verctores longLat poder
				//				// operar con la posicion del dato
				//				Coordinate anchoLongLat = constructCoordinate(alfa,ancho);
				//				Coordinate distanciaLongLat = constructCoordinate(alfa+ Math.PI / 2,distancia);
				//
				//
				//				if(labor.getConfiguracion().correccionDemoraPesadaEnabled()){
				//					Double corrimientoPesada =	labor.getConfiguracion().getCorrimientoPesada();
				//					Coordinate corrimientoLongLat =constructCoordinate(alfa + Math.PI / 2,corrimientoPesada);
				//					// mover el punto 3.5 distancias hacia atras para compenzar el retraso de la pesada
				//
				//					longLatPoint = longLatPoint.getFactory().createPoint(new Coordinate(longLatPoint.getX()+corrimientoPesada*distanciaLongLat.x,longLatPoint.getY()+corrimientoPesada*distanciaLongLat.y));
				//					//utmPoint = utmPoint.getFactory().createPoint(new Coordinate(utmPoint.getX()-corrimientoLongLat.x,utmPoint.getY()-corrimientoLongLat.y));
				//				}
				//
				//				/**
				//				 * creo la geometria que corresponde a la feature tomando en cuenta si esta activado el filtro de distancia y el de superposiciones
				//				 */				
				//				//				Geometry utmGeom = createGeometryForHarvest(anchoLongLat,
				//				//						distanciaLongLat, utmPoint,pasada,altura,ci.getRindeTnHa());		
				//				Geometry longLatGeom = createGeometryForHarvest(anchoLongLat,
				//						distanciaLongLat, longLatPoint,pasada,altura,ci.getRindeTnHa());
				//				if(longLatGeom == null 
				//						//			|| geom.getArea()*ProyectionConstants.A_HAS*10000<labor.config.supMinimaProperty().doubleValue()
				//						){//con esto descarto las geometrias muy chicas
				//					//System.out.println("geom es null, ignorando...");
				//					continue;
				//				}
				//
				//				/**
				//				 * solo ingreso la cosecha al arbol si la geometria es valida
				//				 */
				//				boolean empty = longLatGeom.isEmpty();
				//				boolean valid = longLatGeom.isValid();
				//				boolean big = (longLatGeom.getArea()*ProyectionConstants.A_HAS>supMinimaHas);
				//				if(!empty
				//						&&valid
				//						&&big//esta fallando por aca
				//						){
				//
				//					//Geometry longLatGeom =	crsAntiTransform(utmGeom);//hasta aca se entrega la geometria correctamente
				//
				//					ci.setGeometry(longLatGeom);//FIXME aca ya perdio la orientacion pero tiene la forma correcta
				//				//	corregirRinde(ci);
				//
				//					labor.insertFeature(ci);//featureTree.insert(geom.getEnvelopeInternal(), cosechaFeature);
				//				} else{
				//					//	System.out.println("no inserto el feature "+featureNumber+" porque tiene una geometria invalida empty="+empty+" valid ="+valid+" area="+big+" "+geom);
				//				}

			} else { // no es point. Estoy abriendo una cosecha de poligonos.
				labor.insertFeature(ci);
			}

		}// fin del for que recorre las cosechas por indice
		reader.close();

		List<FertilizacionItem> itemsToShow = new ArrayList<FertilizacionItem>();
		SimpleFeatureIterator it = labor.outCollection.features();
		while(it.hasNext()){
			SimpleFeature f=it.next();
			itemsToShow.add(labor.constructFeatureContainerStandar(f,false));
		}
		it.close();
		labor.constructClasificador();
		runLater(itemsToShow);
		updateProgress(0, featureCount);	
	}

	@Override
	protected ExtrudedPolygon getPathTooltip(Geometry poly, FertilizacionItem fertFeature) {

		double area = poly.getArea() * ProyectionConstants.A_HAS();// 30224432.818;//pathBounds2.getHeight()*pathBounds2.getWidth();
		//double area2 = cosechaFeature.getAncho()*cosechaFeature.getDistancia();
		DecimalFormat df = new DecimalFormat(Messages.getString("ProcessFertMapTask.1")); //$NON-NLS-1$

		String tooltipText = new String(// TODO ver si se puede instalar un
				// boton
				// que permita editar el dato
				Messages.getString("ProcessFertMapTask.2") + df.format(fertFeature.getDosistHa()) //$NON-NLS-1$
				+ Messages.getString("ProcessFertMapTask.3") + Messages.getString("ProcessFertMapTask.4") //$NON-NLS-1$ //$NON-NLS-2$
				+ df.format(fertFeature.getImporteHa()) + Messages.getString("ProcessFertMapTask.5") //$NON-NLS-1$
				//+ "Sup: "
				//+ df.format(area * ProyectionConstants.METROS2_POR_HA)
				//+ " m2\n"
				// +"feature: " + featureNumber
				);
		if(area<1){
			tooltipText=tooltipText.concat( Messages.getString("ProcessFertMapTask.6")+df.format(area * ProyectionConstants.METROS2_POR_HA) + Messages.getString("ProcessFertMapTask.7")); //$NON-NLS-1$ //$NON-NLS-2$
			//	tooltipText=tooltipText.concat( "SupOrig: "+df.format(area2 ) + "m2\n");
		} else {
			tooltipText=tooltipText.concat(Messages.getString("ProcessFertMapTask.8")+df.format(area ) + Messages.getString("ProcessFertMapTask.9")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		//List  paths = 
		return super.getExtrudedPolygonFromGeom(poly, fertFeature,tooltipText);

		//return null;
	}

	//	private ArrayList<Object> getPathTooltip(Polygon poly,
	//			FertilizacionItem fertFeature) {
	//		super.getPathFromGeom2D(poly, cosechaFeature,tooltipText);
	//		
	//		Path path = getPathFromGeom(poly, fertFeature);
	//
	//		double area = poly.getArea() * ProyectionConstants.A_HAS;// 30224432.818;//pathBounds2.getHeight()*pathBounds2.getWidth();
	//		DecimalFormat df = new DecimalFormat("#.00");
	//		String tooltipText = new String(// TODO ver si se puede instalar un
	//										// boton
	//										// que permita editar el dato
	//				"Densidad: " + df.format(fertFeature.getCantFertHa())
	//						+ " Kg/Ha\n" + "Costo: "
	//						+ df.format(fertFeature.getImporteHa()) + " U$S/Ha\n"
	////						+ "Sup: "
	////						+ df.format(area * ProyectionConstants.METROS2_POR_HA)
	////						+ " m2\n"
	//		// +"feature: " + featureNumber
	//		);
	//
	//		if(area<1){
	//			tooltipText=tooltipText.concat( "Sup: "+df.format(area * ProyectionConstants.METROS2_POR_HA) + "m2\n");
	//		} else {
	//			tooltipText=tooltipText.concat("Sup: "+df.format(area ) + "Has\n");
	//		}
	//		
	//		ArrayList<Object> ret = new ArrayList<Object>();
	//		ret.add(path);
	//		ret.add(tooltipText);
	//		return ret;
	//	}

	// TODO devolver un color entre el minimo y el maximo variando el hue entre
	// el hue del rojo hasta el hue del verde

	protected int getAmountMin() {
		return 0;
	}

	protected int gerAmountMax() {
		return 1000;
	}

}// fin del task

