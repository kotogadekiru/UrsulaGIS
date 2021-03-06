package gui;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import dao.Camino;
import dao.Labor;
import dao.LaborItem;
import dao.Ndvi;
import dao.Poligono;
import dao.OrdenDeCompra.OrdenCompra;
import dao.OrdenDeCompra.OrdenCompraItem;
import dao.config.Configuracion;
import dao.cosecha.CosechaConfig;
import dao.cosecha.CosechaLabor;
import dao.fertilizacion.FertilizacionLabor;
import dao.margen.Margen;
import dao.pulverizacion.PulverizacionLabor;
import dao.siembra.SiembraLabor;
import dao.suelo.Suelo;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.data.DataRasterReader;
import gov.nasa.worldwind.data.DataRasterReaderFactory;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import gui.nww.LaborLayer;
import gui.nww.LayerAction;
import gui.nww.LayerPanel;
import gui.nww.WWPanel;
import gui.utils.DateConverter;
import gui.utils.SmartTableView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tasks.ExportLaborMapTask;
import tasks.GetNDVI2ForLaborTask;
import tasks.GetNdviForLaborTask3;
import tasks.GoogleGeocodingHelper;
import tasks.ProcessMapTask;
import tasks.ReadJDHarvestLog;
import tasks.ShowNDVITifFileTask;
import tasks.UpdateTask;
import tasks.crear.ConvertirNdviACosechaTask;
import tasks.crear.CrearCosechaMapTask;
import tasks.crear.CrearFertilizacionMapTask;
import tasks.crear.CrearPulverizacionMapTask;
import tasks.crear.CrearSiembraMapTask;
import tasks.crear.CrearSueloMapTask;
import tasks.crear.GenerarOCTask;
import tasks.importar.OpenMargenMapTask;
import tasks.importar.OpenSoilMapTask;
import tasks.importar.ProcessFertMapTask;
import tasks.importar.ProcessHarvestMapTask;
import tasks.importar.ProcessPulvMapTask;
import tasks.importar.ProcessSiembraMapTask;
import tasks.procesar.CortarCosechaMapTask;
import tasks.procesar.ExportarCosechaDePuntosTask;
import tasks.procesar.ExportarPrescripcionFertilizacionTask;
import tasks.procesar.ExportarPrescripcionSiembraTask;
import tasks.procesar.ExtraerPoligonosDeLaborTask;
import tasks.procesar.GenerarMuestreoDirigidoTask;
import tasks.procesar.GrillarCosechasMapTask;
import tasks.procesar.JuntarShapefilesTask;
import tasks.procesar.ProcessBalanceDeNutrientes;
import tasks.procesar.ProcessMarginMapTask;
import tasks.procesar.RecomendFertNFromHarvestMapTask;
import tasks.procesar.RecomendFertPFromHarvestMapTask;
import tasks.procesar.SiembraFertTask;
import tasks.procesar.SimplificarCaminoTask;
import tasks.procesar.UnirCosechasMapTask;
import tasks.procesar.UnirFertilizacionesMapTask;
import tasks.procesar.UnirSiembrasMapTask;
import utils.DAH;
import utils.ExcelHelper;
import utils.FileHelper;
import utils.ProyectionConstants;

public class JFXMain extends Application {
	private static final String PREFERED_TREE_WIDTH_KEY = "PREFERED_TREE_WIDTH";
	private static final String GOV_NASA_WORLDWIND_AVKEY_INITIAL_ALTITUDE = "gov.nasa.worldwind.avkey.InitialAltitude"; //$NON-NLS-1$
	private static final String GOV_NASA_WORLDWIND_AVKEY_INITIAL_LONGITUDE = "gov.nasa.worldwind.avkey.InitialLongitude"; //$NON-NLS-1$
	private static final String GOV_NASA_WORLDWIND_AVKEY_INITIAL_LATITUDE = "gov.nasa.worldwind.avkey.InitialLatitude"; //$NON-NLS-1$
	//	private static final double MAX_VALUE = 1.0;
	//	private static final double MIN_VALUE = 0.2;
	public static Configuracion config = Configuracion.getInstance();

	public static final String VERSION = "0.2.27"; //$NON-NLS-1$
	public static final String TITLE_VERSION = "Ursula GIS-"+VERSION; //$NON-NLS-1$
	public static final String buildDate = "18/01/2021";
	
	public static final String ICON ="gui/ursula_logo_2020.png";//"gui/32x32-icon-earth.png";// "gui/1-512.png";//UrsulaGIS-Desktop/src/gui/32x32-icon-earth.png //$NON-NLS-1$
	private static final String SOUND_FILENAME = "gui/Alarm08.wav";//"Alarm08.wav" funciona desde eclipse pero no desde el jar  //$NON-NLS-1$


	public static Stage stage=null;
	private Scene scene=null;

	private Dimension canvasSize = new Dimension(1500, 800);

	protected WWPanel wwjPanel=null;
	protected LayerPanel layerPanel=null;
	private VBox progressBox = new VBox();

	//guardo referencia a las capas importadas o generadas
	private List<CosechaLabor> cosechas = new ArrayList<CosechaLabor>();
	private List<FertilizacionLabor> fertilizaciones = new ArrayList<FertilizacionLabor>();
	private List<SiembraLabor> siembras = new ArrayList<SiembraLabor>();
	private List<PulverizacionLabor> pulverizaciones = new ArrayList<PulverizacionLabor>();

	private List<Suelo> suelos = new ArrayList<Suelo>();

	private List<Margen> margenes= new ArrayList<Margen>();

	public static ExecutorService executorPool = Executors.newCachedThreadPool();
	private Node wwNode=null;//contiene el arbol con los layers y el swingnode con el world wind


	@Override
	public void start(Stage primaryStage) throws Exception {
		JFXMain.stage = primaryStage;

		primaryStage.setTitle(TITLE_VERSION);
		primaryStage.getIcons().add(new Image(ICON));

		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

		double ratio = primaryScreenBounds.getHeight()/primaryScreenBounds.getWidth();

		int canvasWidth = (int) (primaryScreenBounds.getWidth()*0.8);
		int canvasHeight = (int) (canvasWidth/ratio);

		canvasSize=new Dimension(canvasWidth,canvasHeight);
		StackPane pane = new StackPane();
		scene = new Scene(pane,canvasSize.getWidth()*1,canvasSize.getHeight()*0.3);//, Color.White);
		primaryStage.setScene(scene);

		addDragAndDropSupport();
		// scene.getStylesheets().add("gisUI/style.css");//esto funciona

		MenuBar menuBar = constructMenuBar();

		setInitialPosition();
		VBox vBox1 = new VBox();
		vBox1.getChildren().add(menuBar);
		createSwingNode(vBox1);
		pane.getChildren().add(vBox1);



		primaryStage.setOnHiding((e)-> {
			Platform.runLater(()->{
				JFXMain.config.save();
				DAH.closeEm();
				System.out.println("em Closed"); //$NON-NLS-1$
				System.out.println("Application Closed by click to Close Button(X)"); //$NON-NLS-1$
				getWwd().shutdown();
				System.exit(0); 
			});
		});
		primaryStage.show();

		//start clearCache cronJob
		startClearCacheCronJob();
	}

	private void startClearCacheCronJob() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		Runnable clearCaches =() ->{
			cosechas.forEach(l->{
				LocalTime now = LocalTime.now().minusSeconds(60);

				synchronized(l){
					if(l.cacheLastRead.isBefore(now)) {
						l.clearCache();
					}
				}
			});
			fertilizaciones.forEach(l->l.clearCache());
			siembras.forEach(l->l.clearCache());
			pulverizaciones.forEach(l->l.clearCache());
			suelos.forEach(l->l.clearCache());
			margenes.forEach(l->l.clearCache());
			// System.out.println("termine de limpiar todas las caches");
		};

		//ScheduledFuture<?> clearCachesHandle =  
				scheduler.scheduleAtFixedRate(clearCaches, 60, 30, TimeUnit.SECONDS);
		//scheduler.schedule(()->  { clearCachesHandle.cancel(true);  }, 60 * 60, TimeUnit.SECONDS);//stop clearing caches after an hour

	}

	private static void setInitialPosition() {
		//Configuracion config =Configuracion.getInstance();
		double initLat = Double.parseDouble(config.getPropertyOrDefault(GOV_NASA_WORLDWIND_AVKEY_INITIAL_LATITUDE, "-35")); //$NON-NLS-1$
		double initLong = Double.parseDouble(config.getPropertyOrDefault(GOV_NASA_WORLDWIND_AVKEY_INITIAL_LONGITUDE, "-62")); //$NON-NLS-1$
		double initAltitude = Double.parseDouble(config.getPropertyOrDefault(GOV_NASA_WORLDWIND_AVKEY_INITIAL_ALTITUDE, "19.07e5")); //$NON-NLS-1$
		//   <Property name="gov.nasa.worldwind.avkey.InitialLatitude" value="-35"/>
		//<Property name="gov.nasa.worldwind.avkey.InitialLongitude" value="-62"/>
		//<Property name="gov.nasa.worldwind.avkey.InitialAltitude" value="19.07e5"/>
		initLat = (initLat>-90&&initLat<90)?initLat:-35.0;
		initLong = (initLong>-180&&initLat<180)?initLat:-62.0;//Chequeo que este entre valores validos
		Configuration.setValue(GOV_NASA_WORLDWIND_AVKEY_INITIAL_LATITUDE, initLat);
		Configuration.setValue(GOV_NASA_WORLDWIND_AVKEY_INITIAL_LONGITUDE,initLong);
		Configuration.setValue(GOV_NASA_WORLDWIND_AVKEY_INITIAL_ALTITUDE, initAltitude);
	}

	public void createSwingNode(VBox vBox1) {
		//		if (Configuration.isMacOS()) {
		//			System.setProperty(
		//					"com.apple.mrj.application.apple.menu.about.name",
		//					JFXMain.TITLE_VERSION);
		//		}

		Task<Node> pfMapTask = new Task<Node>(){
			@Override
			protected Node call() throws Exception {
				try{
					return initializeWorldWind();//esto tiene que estar antes de initialize
					//return wwdNode;
				}catch(Throwable t){
					t.printStackTrace();				
					return null;
				}
			}			
		};

		pfMapTask.setOnSucceeded(handler -> {			
			wwNode = (Node) handler.getSource().getValue();
			if(wwNode!=null) {
				vBox1.getChildren().add( wwNode);
				this.wwjPanel.repaint();	
			}else {
				System.err.println("fallo la iniciacion del worldwind node");
			}

		});
		executorPool.execute(pfMapTask);
	}

	protected Node initializeWorldWind() {
		try {//com.sun.java.swing.plaf.windows.WindowsLookAndFeel
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");//UIManager.getSystemLookAndFeelClassName()); //$NON-NLS-1$
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}

		//	setDefaultSize(50);//esto funciona para la barra de abajo pero no para los placemarks
		try {
		// Create the WorldWindow.
		this.wwjPanel =	new WWPanel(canvasSize, true);
		}catch(Exception e) {
			Platform.runLater(()->{
			Alert a = new Alert(Alert.AlertType.ERROR);
			a.setHeaderText("No se pudo crear WorldWindow");
			String stackTrace = Arrays.toString(e.getStackTrace());
			a.setContentText(stackTrace);
			a.show();
			});
		}
		//una vez que se establecio el tama�o inicial ese es el tama�o maximo
		//this.wwjPanel.setPreferredSize(canvasSize);
		final SwingNode wwSwingNode = new SwingNode();
		wwSwingNode.setContent(wwjPanel);
		// Put the pieces together.
		//wwSwingNode.autosize();
		this.layerPanel = new LayerPanel(this.wwjPanel.getWwd(),stage.widthProperty(),stage.heightProperty());
		this.layerPanel.addToScrollPaneBottom(progressBox);
		//this.layerPanel.setMain(this);

		setAccionesTreePanel();



		JFXMain.stage.heightProperty().addListener((o,old,nu)->{
			this.wwjPanel.setPreferredSize(new Dimension((int)stage.getHeight(),nu.intValue()));
			this.wwjPanel.repaint();
		});

		//ok
		JFXMain.stage.maximizedProperty().addListener((o,ov,nu)->{
			this.wwjPanel.repaint();	
		});

		SplitPane sp = new SplitPane();
		sp.getItems().addAll(layerPanel, wwSwingNode);

		double initSplitPaneWidth = Double.valueOf(JFXMain.config.getPropertyOrDefault(PREFERED_TREE_WIDTH_KEY,Double.toString(stage.getWidth()*0.15f)));

		//me permite agrandar la pantalla sin que se agrande el arbol
		sp.setDividerPositions(initSplitPaneWidth/stage.getWidth());//15% de la pantalla de 1245 es 186px ; 1552.0 es fullscreen
		sp.getDividers().get(0).positionProperty().addListener((o,ov,nu)->{
			//divider position changed to nu
			//System.out.println("changing Width to "+nu);0.12 ~ 0.15 //hanging Width to 1245.0
			double newPreferredSplitPaneWidth = stage.getWidth()*nu.doubleValue();
			JFXMain.config.setProperty(PREFERED_TREE_WIDTH_KEY, Double.toString(newPreferredSplitPaneWidth));
			config.save();
		});

		JFXMain.stage.widthProperty().addListener((o,old,nu)->{
			double splitPaneWidth = Double.valueOf(JFXMain.config.getPropertyOrDefault(PREFERED_TREE_WIDTH_KEY,Double.toString(initSplitPaneWidth)));
			sp.setDividerPositions(splitPaneWidth/nu.doubleValue());
			//System.out.println("changing div to "+splitPaneWidth/nu.doubleValue());//hanging Width to 1245.0
			//15% es 
			this.wwjPanel.setPreferredSize(new Dimension(nu.intValue(),(int)stage.getWidth()));
			this.wwjPanel.repaint();
		});

		// Register a rendering exception listener that's notified when
		// exceptions occur during rendering.
		this.wwjPanel.getWwd().addRenderingExceptionListener(
				new RenderingExceptionListener() {
					public void exceptionThrown(Throwable t) {
						if (t instanceof WWAbsentRequirementException) {
							String message = "Computer does not meet minimum graphics requirements.\n"; //$NON-NLS-1$
							message += "Please install up-to-date graphics driver and try again.\n"; //$NON-NLS-1$
							message += "Reason: " + t.getMessage() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
							message += "This program will end when you press OK."; //$NON-NLS-1$
							System.err.println(message);
							t.printStackTrace();
							Alert alert = new Alert(AlertType.ERROR,message,ButtonType.OK);
							alert.initOwner(stage);
							alert.showAndWait();
							// JOptionPane.showMessageDialog(JFXMain.this,
							// message, "Unable to Start Program",
							// JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
					}
				});

		// Search the layer list for layers that are also select listeners and
		// register them with the World
		// Window. This enables interactive layers to be included without
		// specific knowledge of them here.
		for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers()) {
			if (layer instanceof SelectListener) {
				this.getWwd().addSelectListener((SelectListener) layer);
			}
		}

		importElevations();
		// Center the application on the screen.
		// WWUtil.alignComponent(null, this, AVKey.CENTER);
		stage.setResizable(true);

		// descomentar esto para cargar los poligonos de la base de datos. bloquea la interface
		executorPool.execute(()->loadPoligonos());

		return sp;
	}

	public static void setDefaultSize(int size) {
		Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);

		for (Object key : keys) {
			if (key != null && key.toString().toLowerCase().contains("font")) { //$NON-NLS-1$
				//System.out.println(key);
				Font font = UIManager.getDefaults().getFont(key);
				if (font != null) {
					font = font.deriveFont((float)size);
					UIManager.put(key, font);
				}
			}
		}
	}

	private MenuBar constructMenuBar() {
		/*Menu Importar*/
		final Menu menuImportar = new Menu(Messages.getString("JFXMain.importar")); //$NON-NLS-1$
		//addMenuItem("Suelo",(a)->doOpenSoilMap(),menuImportar);		
		//addMenuItem(Messages.getString("JFXMain.fertilizacion"),(a)->doOpenFertMap(null),menuImportar); //$NON-NLS-1$
		//addMenuItem(Messages.getString("JFXMain.siembra"),(a)->doOpenSiembraMap(null),menuImportar); //$NON-NLS-1$
		//addMenuItem(Messages.getString("JFXMain.pulverizacion"),(a)->doOpenPulvMap(null),menuImportar); //$NON-NLS-1$
		//addMenuItem(Messages.getString("JFXMain.cosecha"),(a)->doOpenCosecha(null),menuImportar);		 //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.NDVI"),(a)->doOpenNDVITiffFiles(),menuImportar); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.imagen"),(a)->importImagery(),menuImportar); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.suelo"),(a)->doOpenSoilMap(null),menuImportar); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.margen"),(a)->doOpenMarginMap(),menuImportar); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.poligonos"),(a)->doImportarPoligonos(null),menuImportar); //$NON-NLS-1$

		final Menu menuHerramientas = new Menu(Messages.getString("JFXMain.herramientas")); //$NON-NLS-1$
		//addMenuItem("CosechaJD",(a)->doLeerCosechaJD(),menuHerramientas);
		//insertMenuItem(menuCalcular,"Retabilidades",a->doProcessMargin());
		addMenuItem(Messages.getString("JFXMain.distancia"),(a)->doMedirDistancia(),menuHerramientas); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.superficie"),(a)->doMedirSuperficie(),menuHerramientas); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.unirShapes"),(a)->JuntarShapefilesTask.process(),menuHerramientas); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.rentabilidad"),(a)->doProcessMargin(),menuHerramientas); //$NON-NLS-1$
		//addMenuItem(Messages.getString("JFXMain.unirCosechas"),(a)->doUnirCosechas(null),menuHerramientas); //$NON-NLS-1$
		//addMenuItem(Messages.getString("JFXMain.unirFertilizaciones"),(a)->doUnirFertilizaciones(),menuHerramientas); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.balanceNutrientes"),(a)->doProcesarBalanceNutrientes(),menuHerramientas); //$NON-NLS-1$
		//addMenuItem(Messages.getString("JFXMain.generarSiembraFert"),(a)->doGenerarSiembraFertilizada(),menuHerramientas); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.generarOrdenCompra"),(a)->doGenerarOrdenDeCompra(),menuHerramientas); //$NON-NLS-1$
		addMenuItem(Messages.getString("JFXMain.goTo"),(a)->showGoToDialog(),menuHerramientas);
		addMenuItem(Messages.getString("JFXMain.bulk_ndvi_download"),(a)->{ //$NON-NLS-1$
			doBulkNDVIDownload();
		},menuHerramientas);

	
		/*Menu Exportar*/
		final Menu menuExportar = new Menu(Messages.getString("JFXMain.exportar"));		 //$NON-NLS-1$
		//	addMenuItem("Suelo",(a)->doExportSuelo(),menuExportar);
		addMenuItem(Messages.getString("JFXMain.exportarPantallaMenuItem"),(a)->doSnapshot(),menuExportar); //$NON-NLS-1$

		/*Menu Configuracion*/
		final Menu menuConfiguracion = (new ShowConfigGUI(this)).contructConfigMenu();

		MenuItem actualizarMI=addMenuItem(Messages.getString("JFXMain.configUpdate"),null,menuConfiguracion); //$NON-NLS-1$
		actualizarMI.setOnAction((a)->doUpdate(actualizarMI));
		actualizarMI.setVisible(false);

		executorPool.submit(()->{
			if(UpdateTask.isUpdateAvailable()){
				actualizarMI.setVisible(true);
				//actualizarMI.getStyleClass().clear();

				actualizarMI.getStyleClass().add("menu-item:focused"); //$NON-NLS-1$
				actualizarMI.setStyle("-fx-background: -fx-accent;" //$NON-NLS-1$
						+"-fx-background-color: -fx-selection-bar;" //$NON-NLS-1$
						+ "-fx-text-fill: -fx-selection-bar-text;" //$NON-NLS-1$
						+ "fx-text-fill: white;"); //$NON-NLS-1$
				//menuConfiguracion.show();
				menuConfiguracion.setStyle("-fx-background: -fx-accent;" //$NON-NLS-1$
						+"-fx-background-color: -fx-selection-bar;" //$NON-NLS-1$
						+ "-fx-text-fill: -fx-selection-bar-text;" //$NON-NLS-1$
						+ "fx-text-fill: white;"); //$NON-NLS-1$

			}
		});

	

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(menuImportar,menuHerramientas, menuExportar,menuConfiguracion);
		menuBar.setPrefWidth(scene.getWidth());
		return menuBar;
	}


	private void setPulverizacionesRootNodeActions() {
		List<LayerAction> rootNodeP = new ArrayList<LayerAction>();
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.importar"),(layer)->{
			this.doOpenPulvMap(null);
			return "opened";	
		})));
		layerPanel.addAccionesClase(rootNodeP,PulverizacionLabor.class);
	}

	private void setFertilizacionesRootNodeActions() {
		List<LayerAction> rootNodeP = new ArrayList<LayerAction>();
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.importar"),(layer)->{
			doOpenFertMap(null);
			return "opened";	
		})));
		
		//addMenuItem(Messages.getString("JFXMain.unirFertilizaciones"),(a)->doUnirFertilizaciones(),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.unirFertilizaciones"),(layer)->{
			doUnirFertilizaciones();
			return "unidas";	
		}),2));
		
		layerPanel.addAccionesClase(rootNodeP,FertilizacionLabor.class);
	}
	
	private void setSiembrasRootNodeActions() {
		List<LayerAction> rootNodeP = new ArrayList<LayerAction>();
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.importar"),(layer)->{
			this.doOpenSiembraMap(null);
			return "opened";	
		})));
		
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.unir"),(layer)->{
			this.doUnirSiembras(null);
			return "joined";	
		}),2));
		
		//addMenuItem(Messages.getString("JFXMain.generarSiembraFert"),(a)->doGenerarSiembraFertilizada(),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.generarSiembraFert"),(layer)->{
			this.doGenerarSiembraFertilizada();
			return "generated";	
		}),1));
		
		
		layerPanel.addAccionesClase(rootNodeP,SiembraLabor.class);
	}
	
	private void setCosechasRootNodeActions() {
		List<LayerAction> rootNodeP = new ArrayList<LayerAction>();
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.importar"),(layer)->{
			this.doOpenCosecha(null);
			return "opened";	
		})));
		
		//addMenuItem(Messages.getString("JFXMain.unirCosechas"),(a)->doUnirCosechas(null),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.unirCosechas"),(layer)->{
			this.doUnirCosechas(null);
			return "joined";	
		}),2));
		
		
		
		
		layerPanel.addAccionesClase(rootNodeP,CosechaLabor.class);
	}
	private void setNdviRootNodeActions() {
		List<LayerAction> rootNodeNDVI = new ArrayList<LayerAction>();
		
		rootNodeNDVI.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.evoNDVI"),(layer)->{ //$NON-NLS-1$
			ShowNDVIEvolution sEvo= new ShowNDVIEvolution(this.getWwd(),this.layerPanel);
			
			sEvo.doShowNDVIEvolution();
			return "mostre la evolucion del ndvi";
		})));
		
		//Exporta todos los ndvi cargados a un archivo excel donde las filas son las coordenadas y las columnas son los valores en esa fecha
		rootNodeNDVI.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.expoNDVI"),(layer)->{ //$NON-NLS-1$
			ExportNDVIToExcel sEvo= new ExportNDVIToExcel(this.getWwd(),this.layerPanel);
			
			sEvo.exportToExcel();
			return "mostre la evolucion del ndvi";
		})));
		
		/**
		 * Save NDVI action
		 * guarda todos los ndvi activos en la rama de ndvi
		 */
		rootNodeNDVI.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.saveAction"),(layer)->{
			executorPool.submit(()->{
				try {
					LayerList layers = this.getWwd().getModel().getLayers();
					for (Layer l : layers) {
						
						Object o =  l.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
						if(o instanceof Ndvi){
							Ndvi ndvi = (Ndvi)o;
							ndvi.updateContent();
							DAH.save(ndvi);
						}
					}
				
				}catch(Exception e) {
					System.err.println("Error al guardar los poligonos"); //$NON-NLS-1$
					e.printStackTrace();
				}
			});
			
			return "Guarde los poligonos"; //$NON-NLS-1$
		})));
		layerPanel.addAccionesClase(rootNodeNDVI,Ndvi.class);
	}
	
	private void setPoligonosRootNodeActions() {
		List<LayerAction> rootNodeP = new ArrayList<LayerAction>();

		//addMenuItem(Messages.getString("JFXMain.distancia"),(a)->doMedirDistancia(),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.distancia"),(layer)->{
			doMedirDistancia();
			return "medida";	
		})));
		
		//addMenuItem(Messages.getString("JFXMain.superficie"),(a)->doMedirSuperficie(),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.superficie"),(layer)->{
			doMedirSuperficie();
			return "medida";	
		})));
		
		//addMenuItem(Messages.getString("JFXMain.unirPoligonos"),(a)->doUnirPoligonos(),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.unirPoligonos"),(layer)->{
			doUnirPoligonos();
			return "unidos";	
		}),2));
		
		//addMenuItem(Messages.getString("JFXMain.intersectarPoligonos"),(a)->doIntersectarPoligonos(),menuHerramientas); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.intersectarPoligonos"),(layer)->{
			doIntersectarPoligonos();
			return "unidas";	
		}),2));
		
		//addMenuItem(Messages.getString("JFXMain.poligonos"),(a)->doImportarPoligonos(null),menuImportar); //$NON-NLS-1$
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.importar"),(layer)->{
			doImportarPoligonos(null);
			return "importados";	
		})));
				
		rootNodeP.add(new LayerAction(constructPredicate(Messages.getString("JFXMain.saveAction"),(layer)->{
			executorPool.submit(()->{
				try {
					LayerList layers = this.getWwd().getModel().getLayers();
					for (Layer l : layers) {
						Object o = l.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
						if (l.isEnabled() && o instanceof Poligono){
							Poligono p = (Poligono)o;
							doGuardarPoligono(p);
						}
					}
				
				}catch(Exception e) {
					System.err.println("Error al guardar los poligonos"); //$NON-NLS-1$
					e.printStackTrace();
				}
			});
			
			return "Guarde los poligonos"; //$NON-NLS-1$
		})));
		
		layerPanel.addAccionesClase(rootNodeP,Poligono.class);
	}
	
	/**
	 * aca se configuran los menues contextuales del arbol de capas
	 //XXX agregar nuevas funcionalidades aca!!! 
	 */
	private void setAccionesTreePanel() {
		setPulverizacionesRootNodeActions();
		setFertilizacionesRootNodeActions();
		setSiembrasRootNodeActions();
		setCosechasRootNodeActions();
		setPoligonosRootNodeActions();
		setNdviRootNodeActions();
		
		Map<Class<?>,List<Function<Layer, String>>> predicates = new HashMap<Class<?>,List<Function<Layer,String>>>();
		addAccionesCosecha(predicates);
		addAccionesFertilizacion(predicates);
		addAccionesSiembras(predicates);	
		addAccionesPulverizaciones(predicates);

		addAccionesPoligonos(predicates);
		addAccionesCaminos(predicates);
		addAccionesNdvi(predicates);

		addAccionesMargen(predicates);
		addAccionesSuelos(predicates);		

		addAccionesLabor(predicates);
		addAccionesGenericas(predicates);

		layerPanel.setMenuItems(predicates);
	}

	private void addAccionesGenericas(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> todosP = new ArrayList<Function<Layer,String>>();
		predicates.put(Object.class, todosP);
		/**
		 * Accion que permite quitar un item del arbol
		 */
		todosP.add(constructPredicate(Messages.getString("JFXMain.removeLayerAction"),(layer)->{

			getWwd().getModel().getLayers().remove(layer);
			Object layerObject =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Labor.class.isAssignableFrom(layerObject.getClass())){
				Labor<?> l = (Labor<?>)layerObject;
				fertilizaciones.remove(l);
				pulverizaciones.remove(l);
				siembras.remove(l);
				cosechas.remove(l);
				suelos.remove(l);
				l.dispose();
			}
			if(layerObject instanceof Poligono){
				Poligono poli = (Poligono) layerObject;
				poli.setActivo(false);
				if(poli.getId()!=null){
					DAH.save(poli);
				}
			}
			if(layerObject instanceof Ndvi){
				Ndvi ndvi = (Ndvi) layerObject;
				ndvi.setActivo(false);
				if(ndvi.getId()!=null){
					DAH.save(ndvi);
				}
			}
			layer.dispose();
			getLayerPanel().update(getWwd());
			return "layer removido" + layer.getName(); //$NON-NLS-1$
		}));
	}

	private void addAccionesSiembras(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> siembrasP = new ArrayList<Function<Layer,String>>();
		predicates.put(SiembraLabor.class, siembrasP);
		/**
		 *Accion que permite editar una siembra
		 */
		siembrasP.add(constructPredicate(Messages.getString("JFXMain.editSiembraAction"),(layer)->{
			doEditSiembra((SiembraLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "siembra editada" + layer.getName(); //$NON-NLS-1$
		}));


		
		/**
		 * Accion permite exportar la labor como shp
		 */
		siembrasP.add(constructPredicate(Messages.getString("JFXMain.exportarSiembraAction"),(layer)->{
			doExportPrescripcionSiembra((SiembraLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "labor Exportada" + layer.getName(); //$NON-NLS-1$
		}));
	}

	private void addAccionesFertilizacion(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> fertilizacionesP = new ArrayList<Function<Layer,String>>();
		predicates.put(FertilizacionLabor.class, fertilizacionesP);
		/**
		 *Accion que permite editar una cosecha
		 */
		fertilizacionesP.add(constructPredicate(Messages.getString("JFXMain.editFertAction"),(layer)->{			
			doEditFertilizacion((FertilizacionLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "fertilizacion editada" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion permite exportar la labor como shp
		 */
		fertilizacionesP.add(constructPredicate(Messages.getString("JFXMain.exportarFertPAction"),(layer)->{			
			doExportPrescripcionFertilizacion((FertilizacionLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "labor Exportada" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion importar Fertilizacion
		 */
		//		fertilizacionesP.add(constructPredicate( Messages.getString("JFXMain.importar"),(layer)->{
		//			doOpenFertMap(null);
		//			//	do((FertilizacionLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
		//				return "labor importada" + layer.getName(); //$NON-NLS-1$
		//			}));
		
	
	//TODO implementar	addMenuItem(Messages.getString("JFXMain.unirFertilizaciones"),(a)->doUnirFertilizaciones(null),menuHerramientas); //$NON-NLS-1$
	}

	private List<Function<Layer, String>> addAccionesPulverizaciones(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> pulverizacionesP = new ArrayList<Function<Layer,String>>();
		predicates.put(PulverizacionLabor.class, pulverizacionesP);
		/**
		 *Accion que permite editar una siembra
		 */
		pulverizacionesP.add(constructPredicate(Messages.getString("JFXMain.editPulvAction"),(layer)->{		
			doEditPulverizacion((PulverizacionLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "pulverizacion editada" + layer.getName(); //$NON-NLS-1$
		}));
		return pulverizacionesP;
	}

	private void addAccionesMargen(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> margenesP = new ArrayList<Function<Layer,String>>();
		predicates.put(Margen.class, margenesP);
		/**
		 *Accion que permite editar un mapa de rentabilidad
		 */
		margenesP.add(constructPredicate(Messages.getString("JFXMain.editMargenAction"),(layer)->{	
			doEditMargin((Margen) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "margen editado" + layer.getName(); //$NON-NLS-1$
		}));
	}

	private void addAccionesSuelos(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> suelosP = new ArrayList<Function<Layer,String>>();
		predicates.put(Suelo.class, suelosP);
		suelosP.add(constructPredicate(Messages.getString("JFXMain.editSoilAction"),(layer)->{	
			doEditSuelo((Suelo) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "suelo editado" + layer.getName(); //$NON-NLS-1$
		}));
	}

	private void addAccionesLabor(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> laboresP = new ArrayList<Function<Layer,String>>();
		predicates.put(Labor.class, laboresP);
		laboresP.add(constructPredicate(Messages.getString("JFXMain.goToLayerAction"),(layer)->{	
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if (layerObject!=null && Labor.class.isAssignableFrom(layerObject.getClass())){
				viewGoTo((Labor<?>) layerObject);
			}
			return "went to " + layer.getName(); //$NON-NLS-1$
		}));

		laboresP.add(constructPredicate(Messages.getString("JFXMain.GuardarLabor"),(layer)->{
			enDesarrollo();
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if (layerObject==null){
			}else if(Labor.class.isAssignableFrom(layerObject.getClass())){
				doGuardarLabor((Labor<?>) layerObject);
			}
			return "guarde labor " + layer.getName();
		}));

		/**
		 * Accion que muesta el histograma
		 */
		laboresP.add(constructPredicate(Messages.getString("JFXMain.showHistogramaLaborAction"),(layer)->{//	this::applyHistogramaCosecha);//(layer)->applyHistogramaCosecha(layer));
			showHistoLabor((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "histograma mostrado" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion que permite extraer los poligonos de una cosecha para guardar
		 */
		laboresP.add(constructPredicate(Messages.getString("JFXMain.extraerPoligonoAction"),(layer)->{
			doExtraerPoligonos((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "poligonos Extraidos " + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion permite exportar la labor como shp
		 */
		laboresP.add((layer)->{
			if(layer==null){
				return Messages.getString("JFXMain.exportLaborAction");  //$NON-NLS-1$
			} else{
				doExportLabor((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
				return "labor Exportada" + layer.getName(); //$NON-NLS-1$
			}});

		/**
		 * Accion muestra una tabla con los datos de la cosecha
		 */
		laboresP.add(constructPredicate(Messages.getString("JFXMain.showTableLayerAction"),(layer)->{
			doShowDataTable((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "Tabla mostrada" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion permite obtener ndvi
		 */
		laboresP.add(constructPredicate(Messages.getString("JFXMain.downloadNDVI"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);			
			if(o instanceof Labor){
				doGetNdviTiffFile(o);
			}
			return "ndvi obtenido" + layer.getName();	 //$NON-NLS-1$
		}));
	}
	
	

	private void addAccionesPoligonos(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> poligonosP = new ArrayList<Function<Layer,String>>();
		predicates.put(Poligono.class, poligonosP);
		
		poligonosP.add(constructPredicate(Messages.getString("JFXMain.editarLayer"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){
				//mostrar un dialogo para editar el nombre del poligono
				Poligono p =(Poligono)layerObject;
				TextInputDialog nombreDialog = new TextInputDialog(p.getNombre());
				nombreDialog.initOwner(stage);
				nombreDialog.setTitle(Messages.getString("JFXMain.editarLayerDialogTitle")); //$NON-NLS-1$
				nombreDialog.setContentText(Messages.getString("JFXMain.editarLayerPoligonName")); //$NON-NLS-1$

				Optional<String> nombreOptional = nombreDialog.showAndWait();
				if(nombreOptional.isPresent()){
					p.setNombre(nombreOptional.get());
					this.getLayerPanel().update(this.getWwd());
				}
			}
			return "edite poligono"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.poligonToSiembraAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){
				//doConvertirASiembra((Polygon) layerObject);
				doCrearSiembra((Poligono) layerObject);
			}
			return "converti a Siembra"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.poligonToFertAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){	
				doCrearFertilizacion((Poligono) layerObject);
			}
			return "converti a Fertilizacion"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.poligonToPulvAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){
				doCrearPulverizacion((Poligono) layerObject);
			}
			return "converti a Pulverizacion"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.poligonToHarvestAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){
				doCrearCosecha((Poligono) layerObject);
			}
			return "converti a Cosecha"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.poligonToSoilAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){
				doCrearSuelo((Poligono) layerObject);
				layer.setEnabled(false);
				//TODO deshabilitar layer para que el polygono no rompa la cosecha nueva
			}
			return "converti a Suelo"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.saveAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Poligono.class.isAssignableFrom(layerObject.getClass())){
				doGuardarPoligono((Poligono) layerObject);
			}
			return "Guarde Guarde"; //$NON-NLS-1$
		}));

		poligonosP.add(constructPredicate(Messages.getString("JFXMain.goToPoligonoAction"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if (layerObject==null){
			}else if(Poligono.class.isAssignableFrom(layerObject.getClass())){
				Poligono poli = (Poligono)layerObject;
				Position pos =poli.getPositions().get(0);
				viewGoTo(pos);
			}
			return "went to " + layer.getName(); //$NON-NLS-1$
		}));
		poligonosP.add(constructPredicate(Messages.getString("JFXMain.downloadNDVIAction"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);			
			if(o instanceof Poligono){
				doGetNdviTiffFile(o);
			}
			return "ndvi obtenido" + layer.getName();	 //$NON-NLS-1$
		}));
		
		//TODO implementar addMenuItem(Messages.getString("JFXMain.unirPoligonos"),(a)->doUnirPoligonos(),menuHerramientas); //$NON-NLS-1$
		//TODO implementar addMenuItem(Messages.getString("JFXMain.intersectarPoligonos"),(a)->doIntersectarPoligonos(),menuHerramientas); //$NON-NLS-1$
		
	}
	
	private void addAccionesCaminos(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> poligonosP = new ArrayList<Function<Layer,String>>();
		predicates.put(Camino.class, poligonosP);
		
		poligonosP.add(constructPredicate(Messages.getString("JFXMain.editarLayer"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Camino.class.isAssignableFrom(layerObject.getClass())){
				//mostrar un dialogo para editar el nombre del poligono
				Camino p =(Camino)layerObject;
				TextInputDialog nombreDialog = new TextInputDialog(p.getNombre());
				nombreDialog.initOwner(stage);
				nombreDialog.setTitle(Messages.getString("JFXMain.editarLayerDialogTitle")); //$NON-NLS-1$
				nombreDialog.setContentText(Messages.getString("JFXMain.editarLayerPoligonName")); //$NON-NLS-1$

				Optional<String> nombreOptional = nombreDialog.showAndWait();
				if(nombreOptional.isPresent()){
					p.setNombre(nombreOptional.get());
					this.getLayerPanel().update(this.getWwd());
				}
			}
			return "edite camino"; //$NON-NLS-1$
		}));
		
		poligonosP.add(constructPredicate(Messages.getString("JFXMain.acortarCamino"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Camino.class.isAssignableFrom(layerObject.getClass())){
				//mostrar un dialogo para editar el nombre del poligono
				Camino camino =(Camino)layerObject;
				
				SimplificarCaminoTask t = new SimplificarCaminoTask(camino);
				t.run();				
				MeasureTool measureTool = PathLayerFactory.createCaminoLayer(camino, this.getWwd(), this.getLayerPanel());
				measureTool.setArmed(false);

				insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
				this.getLayerPanel().update(this.getWwd());
			}
			return "acorte camino"; //$NON-NLS-1$
		}));
	}
	
	private List<Function<Layer, String>> addAccionesCosecha(
			Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> cosechasP = new ArrayList<Function<Layer,String>>();
		predicates.put(CosechaLabor.class, cosechasP);

		/**
		 *Accion que permite editar una cosecha
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.editCosechaAction"),(layer)->{
			doEditCosecha((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "cosecha editada" + layer.getName(); //$NON-NLS-1$

		}));

		/**
		 * Accion que permite pasar una grilla sobre la cosecha
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.grillarCosechaAction"),(layer)->{
			doGrillarCosechas((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "cosecha editada" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion que permite pasar una grilla sobre la cosecha
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.clonarCosechaAction"),(layer)->{
			doUnirCosechas((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "cosecha clonada" + layer.getName(); //$NON-NLS-1$
		}));
		
		/**
		 * Accion que permite pasar una grilla sobre la cosecha
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.cortarCosechaAction"),(layer)->{
			doCortarCosecha((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "cosecha cortada" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion que muesta el la relacion entre el rinde y la elevacion
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.showHeightVsAmountChart"),(layer)->{
			showAmountVsElevacionChart((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "grafico mostrado " + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion que permite generar un muestreo dirigido para los poligonos de la cosecha
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.generarMuestreoDirigido"),(layer)->{
			doGenerarMuestreoDirigido((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "muestreo dirigido " + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion permite crear una fertilizacion P para reponer lo extraido por la cosecha
		 */
		cosechasP.add((layer)->{
			if(layer==null){
				return Messages.getString("JFXMain.recompendarFertP");  //$NON-NLS-1$
			} else{
				doRecomendFertPFromHarvest((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
				return "Fertilizacion P Creada" + layer.getName(); //$NON-NLS-1$
			}});

		/**
		 * Accion permite crear una fertilizacion P para reponer lo extraido por la cosecha
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.recomendarFertN"),(layer)->{
			doRecomendFertNFromHarvest((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "Fertilizacion N Creada" + layer.getName(); //$NON-NLS-1$
		}));

		/**
		 * Accion permite exportar la cosecha como shp de puntos
		 */
		cosechasP.add(constructPredicate(Messages.getString("JFXMain.exportarCosechaAPuntosAction"),(layer)->{
			doExportHarvestDePuntos((CosechaLabor) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
			return "cosecha exportada como puntos: " + layer.getName(); //$NON-NLS-1$
		}));
		
		//TODO implementar addMenuItem(Messages.getString("JFXMain.unirCosechas"),(a)->doUnirCosechas(null),menuHerramientas); //$NON-NLS-1$
		return cosechasP;
	}

	private void addAccionesNdvi(Map<Class<?>, List<Function<Layer, String>>> predicates) {
		List<Function<Layer, String>> ndviP = new ArrayList<Function<Layer,String>>();
		predicates.put(Ndvi.class, ndviP);
		
		ndviP.add(constructPredicate(Messages.getString("JFXMain.editarLayer"),(layer)->{
			Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(layerObject!=null && Ndvi.class.isAssignableFrom(layerObject.getClass())){
				//mostrar un dialogo para editar el nombre del poligono
				Ndvi ndvi =(Ndvi)layerObject;
				TextInputDialog nombreDialog = new TextInputDialog(ndvi.getNombre());
				nombreDialog.initOwner(stage);
				nombreDialog.setTitle(Messages.getString("JFXMain.editarLayerDialogTitle")); //$NON-NLS-1$
				nombreDialog.setContentText(Messages.getString("JFXMain.editarLayerNDVIName")); //$NON-NLS-1$

				Optional<String> nombreOptional = nombreDialog.showAndWait();
				if(nombreOptional.isPresent()){
					ndvi.setNombre(nombreOptional.get());
					DecimalFormat df = new DecimalFormat(Messages.getString("GenerarMuestreoDirigidoTask.5")); //$NON-NLS-1$
					layer.setName(ndvi.getNombre()+" "+df.format(ndvi.getPorcNubes()*100)+"% Nublado");
					this.getLayerPanel().update(this.getWwd());
				}
			}
			return "edite ndvi"; //$NON-NLS-1$
		}));
		
		ndviP.add(constructPredicate(Messages.getString("JFXMain.convertirNDVIaCosechaAction"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(o instanceof Ndvi){
				doConvertirNdviACosecha((Ndvi) o);
			}

			return "rinde estimado desde ndvi" + layer.getName(); //$NON-NLS-1$
		}));

		

		ndviP.add(constructPredicate(Messages.getString("JFXMain.mostrarNDVIChartAction"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(o instanceof Ndvi){
				showHistoNDVI((Ndvi)o);
			}

			return "histograma ndvi mostrado" + layer.getName(); //$NON-NLS-1$
		}));

		ndviP.add(constructPredicate(Messages.getString("JFXMain.goToNDVIAction"),(layer)->{
			Object zoomPosition = layer.getValue(ProcessMapTask.ZOOM_TO_KEY);		

			//Object layerObject = layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if (zoomPosition==null){
			}else if(zoomPosition instanceof Position){
				Position pos =(Position)zoomPosition;
				viewGoTo(pos);
			}
			return "went to " + layer.getName(); //$NON-NLS-1$
		}));

		ndviP.add(constructPredicate(Messages.getString("JFXMain.guardarNDVIAction"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(o instanceof Ndvi){
				Ndvi ndvi = (Ndvi)o;
				ndvi.updateContent();
				DAH.save(ndvi);
			}

			return "guarde" + layer.getName(); //$NON-NLS-1$
		}));

		/*
		 * funcionalidad que permite guardar el archivo tiff de este ndvi en una ubicacion definida por el usuario
		 */
		ndviP.add(constructPredicate(Messages.getString("JFXMain.exportarNDVItoTIFFAction"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(o instanceof Ndvi){
				Ndvi ndvi = (Ndvi)o;
				doExportarTiffFile(ndvi);
			}

			return "exporte" + layer.getName(); //$NON-NLS-1$
		}));
		
		
		/*
		 * funcionalidad que permite guardar el archivo tiff de este ndvi en una ubicacion definida por el usuario
		 */
		ndviP.add(constructPredicate(Messages.getString("JFXMain.expoNDVIToKML"),(layer)->{
			Object o =  layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if(o instanceof Ndvi){
				Ndvi ndvi = (Ndvi)o;
				ExportNDVIToKMZ toKMZ= new ExportNDVIToKMZ(this.getWwd(),this.layerPanel);
				try {
					toKMZ.exportToKMZ(ndvi);
				} catch (XMLStreamException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			return "exporte" + layer.getName(); //$NON-NLS-1$
		}));
		
		//TODO implementar addMenuItem(Messages.getString("JFXMain.evoNDVI"),(a)->{			doShowNDVIEvolution();		},menuHerramientas);
	
	}



	private Function<Layer, String> constructPredicate(String name,Function<Layer, String> action) {
		return (layer)->{
			if(layer==null)return name;
			return action.apply(layer);
		};
	}

	private void doExportarTiffFile(Ndvi ndvi) {
		ndvi.updateContent();

		File tiffFile = ndvi.getF();
		File dir =FileHelper.getNewTiffFile(tiffFile.getName());
		try{
			InputStream in = new FileInputStream(tiffFile);
			OutputStream out = new FileOutputStream(dir);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}











	private void enDesarrollo() {
		Alert enDesarrollo = new Alert(AlertType.INFORMATION,Messages.getString("JFXMain.workInProgressAction")); //$NON-NLS-1$
		enDesarrollo.showAndWait();
	}


	//	private String applyHistogramaCosecha(Layer layer){
	//		if(layer==null){
	//			return Messages.getString("JFXMain.showHistogramaLaborAction");  //$NON-NLS-1$
	//		} else{
	//			showHistoLabor((Labor<?>) layer.getValue(Labor.LABOR_LAYER_IDENTIFICATOR));
	//			return "histograma mostrado" + layer.getName(); //$NON-NLS-1$
	//		}
	//	}

	private void importElevations(){


		executorPool.execute(()->{
			//try{
			// Download the data and save it in a temp file.
			//  File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH, ".tif");

			// Create a local elevation model from the data.
			final ElevationModel elevationModel = new ZeroElevationModel(){
				//            	 @Override
				//            	    public double getElevation(Angle latitude, Angle longitude){
				//            		 return 0;
				//            	    	
				//            	    }
			};
			//  elevationModel.addElevations(sourceFile);

			//				}catch (Exception e){
			//					e.printStackTrace();
			//				}
			// Get the WorldWindow's current elevation model.
			Globe globe = getWwd().getModel().getGlobe();
			//ElevationModel currentElevationModel = globe.getElevationModel();

			// Add the new elevation model to the globe.
			//				if (currentElevationModel instanceof CompoundElevationModel)
			//					((CompoundElevationModel) currentElevationModel).addElevationModel(elevationModel);
			//				else
			globe.setElevationModel(elevationModel);

			// Set the view to look at the imported elevations, although they might be hard to detect. To
			// make them easier to detect, replace the globe's CompoundElevationModel with the new elevation
			// model rather than adding it.
			//     Sector modelSector = elevationModel.getSector();
			//   ExampleUtil.goTo(getWwd(), modelSector);
		});

	}


	public WorldWindow getWwd() {
		return this.wwjPanel.getWwd();
	}

	public StatusBar getStatusBar() {
		return this.wwjPanel.getStatusBar();
	}

	public LayerPanel getLayerPanel() {
		return layerPanel;
	}

	//	public StatisticsPanel getStatsPanel() {
	//		return statsPanel;
	//	}

	//	public void setToolTipController(ToolTipController controller) {
	//		if (this.wwjPanel.toolTipController != null)
	//			this.wwjPanel.toolTipController.dispose();
	//
	//		this.wwjPanel.toolTipController = controller;
	//	}

	//	public void setHighlightController(HighlightController controller) {
	//		if (this.wwjPanel.highlightController != null)
	//			this.wwjPanel.highlightController.dispose();
	//
	//		this.wwjPanel.highlightController = controller;
	//	}

	public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
		// Insert the layer into the layer list just before the compass.
		int compassPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers) {
			if (l instanceof CompassLayer)
				compassPosition = layers.indexOf(l);
		}
		layers.add(compassPosition, layer);
	}

	//	public static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
	//		// Insert the layer into the layer list just before the placenames.
	//		int compassPosition = 0;
	//		LayerList layers = wwd.getModel().getLayers();
	//		for (Layer l : layers) {
	//			if (l instanceof PlaceNameLayer)
	//				compassPosition = layers.indexOf(l);
	//		}
	//		layers.add(compassPosition, layer);
	//	}

	//	public static void insertAfterPlacenames(WorldWindow wwd, Layer layer) {
	//		// Insert the layer into the layer list just after the placenames.
	//		int compassPosition = 0;
	//		LayerList layers = wwd.getModel().getLayers();
	//		for (Layer l : layers) {
	//			if (l instanceof PlaceNameLayer)
	//				compassPosition = layers.indexOf(l);
	//		}
	//		layers.add(compassPosition + 1, layer);
	//	}

	//	public static void insertBeforeLayerName(WorldWindow wwd, Layer layer,
	//			String targetName) {
	//		// Insert the layer into the layer list just before the target layer.
	//		int targetPosition = 0;
	//		LayerList layers = wwd.getModel().getLayers();
	//		for (Layer l : layers) {
	//			if (l.getName().indexOf(targetName) != -1) {
	//				targetPosition = layers.indexOf(l);
	//				break;
	//			}
	//		}
	//		layers.add(targetPosition, layer);
	//	}

	static {
		System.setProperty("java.net.useSystemProxies", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		if (Configuration.isMacOS()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name", //$NON-NLS-1$
					"World Wind Application"); //$NON-NLS-1$
			System.setProperty("com.apple.mrj.application.growbox.intrudes", //$NON-NLS-1$
					"false"); //$NON-NLS-1$
			System.setProperty("apple.awt.brushMetalLook", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (Configuration.isWindowsOS()) {
			System.setProperty("sun.awt.noerasebackground", "true");  //$NON-NLS-1$ //$NON-NLS-2$
			// prevents
			// flashing
			// during
			// window
			// resizing
		}
	}


	private void doLeerCosechaJD() {
		//TODO empezar por el fdl y leer el nombre del archivo y el formato del archivo fdd
		/**
		 <LogDataBlock logDataProcessor="EvenByteDelta">
			<EvenByteDelta filePathName="50b2706b-0000-1000-7fdb-e1e1e114c450.fdd"
				xmlns="urn:schemas-johndeere-com:LogDataBlock:EvenByteDelta"
				filePosition="0" />
		</LogDataBlock>
		 */
		List<File> files =FileHelper.chooseFiles("FDL", "*.fdl"); //$NON-NLS-1$ //$NON-NLS-2$
		ReadJDHarvestLog task = new ReadJDHarvestLog(files.get(0));
		task.installProgressBar(progressBox);

		task.setOnSucceeded(handler -> {
			//File ret = (File)handler.getSource().getValue();

			task.uninstallProgressBar();


			System.out.println("ReadJDHarvestLog succeded"); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded						
		//umTask.start();					
		//this.executorPool.execute(task);

		executorPool.submit(task);
	}

	/**
	 * metodo que toma las labores activas de siembra fertilizacion y pulverizacion y hace una lista con los insumos y cantidades para
	 * cotizar precios online. Permite exporta a excel y cotizar precios online y guardar
	 */
	private void doGenerarOrdenDeCompra() {
		enDesarrollo();
		Alert message = new Alert(Alert.AlertType.INFORMATION);
		message.setHeaderText(Messages.getString("JFXMain.generarOrdenCompraAction")); //$NON-NLS-1$
		message.setContentText(Messages.getString("JFXMain.gOC1") //$NON-NLS-1$
				+ Messages.getString("JFXMain.gOC2") //$NON-NLS-1$
				+ Messages.getString("JFXMain.gOC3")); //$NON-NLS-1$
		message.show();
		//TODO implementar este metodo
		
		GenerarOCTask gOCTask = new GenerarOCTask(getSiembrasSeleccionadas(),getFertilizacionesSeleccionadas(), getPulverizacionesSeleccionadas());

		gOCTask.installProgressBar(progressBox);

		gOCTask.setOnSucceeded(handler -> {
			OrdenCompra ret = (OrdenCompra)handler.getSource().getValue();
			gOCTask.uninstallProgressBar();

			playSound();
			doShowOrdenCompra(ret);
		
			System.out.println("SiembraFertTask succeded"); //$NON-NLS-1$
		});
		executorPool.execute(gOCTask);
	}

	private void doShowOrdenCompra(OrdenCompra ret) {
		Platform.runLater(()->{
			final ObservableList<OrdenCompraItem> data =
					FXCollections.observableArrayList(
							ret.getItems()
							);
			
			SmartTableView<OrdenCompraItem> table = new SmartTableView<OrdenCompraItem>(data,
					Arrays.asList("Id"),
					Arrays.asList("Producto","Cantidad")
					);
			table.setEditable(true);
			//			table.setOnDoubleClick(()->new Poligono());
//			table.setOnShowClick((ndvi)->{
//				//poli.setActivo(true);
//				doShowNDVI(ndvi);
//
//			});

			VBox v=new VBox();
			v.getChildren().add(table);
			HBox h = new HBox();
			Button guardarB = new Button("Guardar");
			guardarB.setOnAction(actionEvent->{
				System.out.println("implementar GuardarOrden de compra action");
				DAH.save(ret);
			});
			Button exportarB = new Button("Exportar");
			exportarB.setOnAction(actionEvent->{
				ExcelHelper helper = new ExcelHelper();
				helper.exportOrdenCompra(ret);
			});
			
			Button cotizarOblineB = new Button("Cotizar OnLine");
			cotizarOblineB.setOnAction(actionEvent->{
				
			});
			h.getChildren().addAll(guardarB,exportarB,cotizarOblineB);
			
			v.getChildren().add(h);
			Scene scene = new Scene(v, 800, 600);
			Stage tablaStage = new Stage();
			tablaStage.getIcons().add(new Image(JFXMain.ICON));
			tablaStage.setTitle(Messages.getString("JFXMain.OrdenCompra")); //$NON-NLS-1$
			tablaStage.setScene(scene);

			tablaStage.onHiddenProperty().addListener((o,old,n)->{
				this.getLayerPanel().update(this.getWwd());
				//getWwd().redraw();
			});

			tablaStage.show();	 
		});	
		
	}

	private List<FertilizacionLabor> getFertilizacionesSeleccionadas() {
		return fertilizaciones.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
	}

	private List<PulverizacionLabor> getPulverizacionesSeleccionadas() {
		return pulverizaciones.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
	}

	private List<SiembraLabor> getSiembrasSeleccionadas() {
		return siembras.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
	}
	
	private List<CosechaLabor> getCosechasSeleccionadas() {
		return cosechas.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
	}
	
	
	
	
	private void doGenerarSiembraFertilizada() {
		//enDesarrollo();
//		Alert message = new Alert(Alert.AlertType.INFORMATION);
//		message.setHeaderText(Messages.getString("JFXMain.generarSiembraFertAction")); //$NON-NLS-1$
//		message.setContentText(Messages.getString("JFXMain.generarSiembraFertText")  //$NON-NLS-1$
//				+ Messages.getString("JFXMain.generarSiembraFertText2")); //$NON-NLS-1$
//		message.show();

		//TODO si son mas de una ver de unirlas primero
		SiembraLabor siembraEnabled = getSiembrasSeleccionadas().get(0);
	
		FertilizacionLabor fertEnabled = getFertilizacionesSeleccionadas().get(0);
	

		SiembraFertTask siembraFertTask = new SiembraFertTask(siembraEnabled, fertEnabled);

		siembraFertTask.installProgressBar(progressBox);

		siembraFertTask.setOnSucceeded(handler -> {
			SiembraLabor ret = (SiembraLabor)handler.getSource().getValue();
			siembraFertTask.uninstallProgressBar();

			this.siembras.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());

			playSound();
			viewGoTo(ret);
			System.out.println("SiembraFertTask succeded"); //$NON-NLS-1$
		});
		executorPool.execute(siembraFertTask);


	}



	

	private void doUpdate(MenuItem actualizarMI) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setContentText(Messages.getString("JFXMain.doUpdateText")); //$NON-NLS-1$
		alert.initOwner(JFXMain.stage);
		alert.showAndWait();
		if(ButtonType.OK.equals(alert.getResult())){
			UpdateTask uTask = new UpdateTask();
			uTask.installProgressBar(progressBox);
			uTask.setOnSucceeded(handler -> {
				File newVersion = (File) handler.getSource().getValue();	
				if(newVersion==null){
					Alert error = new Alert(Alert.AlertType.ERROR);
					error.setContentText(Messages.getString("JFXMain.doUpdateErrorText")); //$NON-NLS-1$
					error.initOwner(JFXMain.stage);
					error.showAndWait();
				} else{
					Alert error = new Alert(Alert.AlertType.CONFIRMATION);
					error.setContentText(Messages.getString("JFXMain.doUpdateSuccessText")); //$NON-NLS-1$
					error.initOwner(JFXMain.stage);
					error.showAndWait();

					//					playSound();	
					//					actualizarMI.getParentMenu().setStyle("");
					//					actualizarMI.setVisible(false);
					//borrar estilos del menuConfiguracion

					// cerrar esta app y abrir la nueva?
				}
				//insertBeforeCompass(getWwd(), ndviLayer);
				//this.getLayerPanel().update(this.getWwd());
				//viewGoTo(ndviLayer);
				uTask.uninstallProgressBar();

			});
			//	Platform.runLater(uTask);

			executorPool.submit(uTask);


		}//fin del if OK	
	}


	private void loadPoligonos(){
		List<Poligono> poligonos = DAH.getPoligonosActivos();
		showPoligonos(poligonos);

		List<Ndvi> ndviActivos = DAH.getNdviActivos();
		for(Ndvi ndvi : ndviActivos){
			//System.out.println("mostrando ndvi activo");
			doShowNDVI(ndvi);
		}
	}

	public void showPoligonos(List<Poligono> poligonos) {
		for(Poligono poli : poligonos){
			MeasureTool measureTool = PoligonLayerFactory.createPoligonLayer(poli, this.getWwd(), this.getLayerPanel());
			Platform.runLater(()->{
				insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
				this.getLayerPanel().update(this.getWwd());
			});
		}
	}


	private void doMedirSuperficie(){
		Poligono poli = new Poligono();
		MeasureTool measureTool = PoligonLayerFactory.createPoligonLayer(poli, this.getWwd(), this.getLayerPanel());
		measureTool.setArmed(true);

		insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
		this.getLayerPanel().update(this.getWwd());


		Alert supDialog = new Alert(Alert.AlertType.INFORMATION);
		supDialog.initOwner(JFXMain.stage);
		supDialog.setTitle(Messages.getString("JFXMain.medirSuperficieDielogTitle")); //$NON-NLS-1$
		supDialog.setHeaderText(Messages.getString("JFXMain.medirSuperficieHeaderText")); //$NON-NLS-1$

		Text t = new Text();
		TextField nombreTF = new TextField();
		nombreTF.setPromptText(Messages.getString("JFXMain.medirSuperficieNombreLabel")); //$NON-NLS-1$
		VBox vb = new VBox();
		vb.getChildren().addAll(nombreTF,t);
		supDialog.setGraphic(vb);
		supDialog.initModality(Modality.NONE);
		nombreTF.textProperty().addListener((obj,old,n)->{
			poli.setNombre(n);
			// es importante para que se modifique el layerPanel con el nombre actualizado
			this.getLayerPanel().update(this.getWwd());
		});

		supDialog.show();
		supDialog.setOnHidden((event)->{			
			measureTool.setArmed(false);
		});
	}
	

	private void doMedirDistancia(){
		Camino camino = new Camino();
		MeasureTool measureTool = PathLayerFactory.createCaminoLayer(camino, this.getWwd(), this.getLayerPanel());
		measureTool.setArmed(true);

		insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
		this.getLayerPanel().update(this.getWwd());	

		Alert supDialog = new Alert(Alert.AlertType.INFORMATION);
		supDialog.initOwner(JFXMain.stage);
		supDialog.setTitle(Messages.getString("JFXMain.medirDistancia")); //$NON-NLS-1$
		supDialog.setHeaderText(Messages.getString("JFXMain.medirDistanciaHeaderText")); //$NON-NLS-1$

		Text t = new Text();
		TextField nombreTF = new TextField();
		nombreTF.setPromptText(Messages.getString("JFXMain.medirDistanciaHeaderText")); //$NON-NLS-1$
		VBox vb = new VBox();
		vb.getChildren().addAll(nombreTF,t);
		supDialog.setGraphic(vb);
		supDialog.initModality(Modality.NONE);
		nombreTF.textProperty().addListener((obj,old,n)->{
			camino.setNombre(n);
			// es importante para que se modifique el layerPanel con el nombre actualizado
			this.getLayerPanel().update(this.getWwd());
		});

		supDialog.show();
		supDialog.setOnHidden((event)->{			
			measureTool.setArmed(false);
		});
	}

	/**
	 * metodo que toma los poligonos seleccionados y los une si se intersectan
	 */
	private void doUnirPoligonos(){
		List<Geometry> geometriasActivas = new ArrayList<Geometry>();
		//1 obtener los poligonos activos
		LayerList layers = this.getWwd().getModel().getLayers();
		for (Layer l : layers) {
			Object o = l.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if (l.isEnabled() && o instanceof Poligono){
				Poligono p = (Poligono)o;
				geometriasActivas.add(p.toGeometry());
			}
		}

		GeometryFactory fact = new GeometryFactory();
		Geometry[] geomArray = new Geometry[geometriasActivas.size()];
		for(int i=0;i<geometriasActivas.size();i++){
			geomArray[i]=geometriasActivas.get(i);
		}
		GeometryCollection collection = fact.createGeometryCollection(geomArray);
		Geometry union = collection.buffer(0);//ProyectionConstants.metersToLongLat(20));
		double has = ProyectionConstants.A_HAS(union.getArea());
		Poligono poli = ExtraerPoligonosDeLaborTask.geometryToPoligono(union);
		MeasureTool measureTool = PoligonLayerFactory.createPoligonLayer(poli, this.getWwd(), this.getLayerPanel());
		poli.setArea(has);
		poli.setNombre(Messages.getString("JFXMain.poligonUnionNamePrefixText")); //$NON-NLS-1$
		insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
		this.getLayerPanel().update(this.getWwd());		
	}

	/**
	 * metodo que toma los poligonos seleccionados calcula la inteseccion y agrega
	 * los poligonos intesectados
	 * a int b
	 * a - (a int b)
	 * b - (a int b)
	 * 
	 */
	private void doIntersectarPoligonos(){
		executorPool.submit(()->{
			try {
				List<Geometry> geometriasActivas = new ArrayList<Geometry>();
				//1 obtener los poligonos activos
				LayerList layers = this.getWwd().getModel().getLayers();
				for (Layer l : layers) {
					Object o = l.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
					if (l.isEnabled() && o instanceof Poligono){
						Poligono p = (Poligono)o;
						geometriasActivas.add(p.toGeometry());
					}
				}
				List<Geometry> geometriasOutput = new ArrayList<Geometry>();
				for(int i=0;i<geometriasActivas.size()-1;i++){
					Geometry a = geometriasActivas.get(i);
					for(int j = i+1;j<geometriasActivas.size();j++){
						Geometry b =geometriasActivas.get(j);
						try {geometriasOutput.add(a.difference(b));}catch(Exception e) {}//found non-noded intersection between LINESTRING (
						try {geometriasOutput.add(b.difference(a));}catch(Exception e) {}
						try {geometriasOutput.add(a.intersection(b));}catch(Exception e) {}						
					}
				}

				int num=0;
				for(Geometry g:geometriasOutput){
					for(int n=0;n<g.getNumGeometries();n++){
						Geometry gn=g.getGeometryN(n);
						if(g.getNumGeometries()==0)continue;
						Poligono poli = ExtraerPoligonosDeLaborTask.geometryToPoligono(gn);
						if(poli ==null)continue;
						MeasureTool measureTool = PoligonLayerFactory.createPoligonLayer(poli, this.getWwd(), this.getLayerPanel());
						double has = ProyectionConstants.A_HAS(gn.getArea());
						poli.setArea(has);
						poli.setNombre(Messages.getString("JFXMain.poligonIntersectionNamePrefix")+num+"["+n+"]");num++; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
					}
				}

				this.getLayerPanel().update(this.getWwd());
			}catch(Exception e) {
				System.err.println("Error al intesectar los poligonos"); //$NON-NLS-1$
				e.printStackTrace();
			}
		});
	}



	/**
	 * Funcion util para vincular un metodo con un item en un menu
	 * @param name nombre para mostrar en el menu
	 * @param action metodo a vincular con el item
	 * @param parent menu en el que se va a insertar el menuItem
	 */
	private static MenuItem addMenuItem(String name, EventHandler<ActionEvent> action, Menu parent){
		MenuItem menuItemProductos = new MenuItem(name);
		menuItemProductos.setOnAction(action);
		parent.getItems().addAll(menuItemProductos);
		return menuItemProductos;
	}


	private void showGoToDialog() {
		TextInputDialog anchoDialog = new TextInputDialog(Messages.getString("JFXMain.goToExample")); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.goToDialogTitle")); //$NON-NLS-1$
		anchoDialog.setHeaderText(Messages.getString("JFXMain.goToDialogHeader")); //$NON-NLS-1$
		anchoDialog.initOwner(JFXMain.stage);
		Optional<String> anchoOptional = anchoDialog.showAndWait();
		if(anchoOptional.isPresent()){
			Position pos = GoogleGeocodingHelper.obtenerPositionDirect(anchoOptional.get());
			if(pos!=null){
				viewGoTo(pos);
			}				
		} else{
			return;
		}
	}
	
	public void viewGoTo(Labor<?> labor) {
		viewGoTo(labor.getLayer());
	}

	public void viewGoTo(Layer layer) {
		try {
			Position position=(Position) layer.getValue(ProcessMapTask.ZOOM_TO_KEY);
			viewGoTo(position);
		} catch (Exception e) {
			System.err.println("fallo hacer zoom a la cosecha nueva"); //$NON-NLS-1$
			e.printStackTrace();
		}
	}
	public void viewGoTo(Position position) {
		if(position==null) return;
		//Configuracion config =Configuracion.getInstance();
		config.setProperty(GOV_NASA_WORLDWIND_AVKEY_INITIAL_LATITUDE, String.valueOf(position.getLatitude().degrees));
		config.setProperty(GOV_NASA_WORLDWIND_AVKEY_INITIAL_LONGITUDE,String.valueOf(position.getLongitude().degrees));
		config.setProperty(GOV_NASA_WORLDWIND_AVKEY_INITIAL_ALTITUDE, "64000"); //$NON-NLS-1$
		config.save();
		View view =getWwd().getView();
		view.goTo(position, 3000d);
	}

	private void showAmountVsElevacionChart(Labor<?> cosechaLabor) {
		TextInputDialog anchoDialog = new TextInputDialog("20"); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.heightVsAmountDialogTitle")); //$NON-NLS-1$
		anchoDialog.setContentText(Messages.getString("JFXMain.heightVsAmountDialogMaxGroupsText")); //$NON-NLS-1$
		anchoDialog.initOwner(JFXMain.stage);
		Optional<String> oGrupos = anchoDialog.showAndWait();
		int grupos=Integer.parseInt(oGrupos.get());
		Labor<?>[] cosechasAux = new Labor[]{cosechaLabor};
		if(cosechaLabor==null){
			Optional<CosechaLabor> optional = HarvestSelectDialogController.select(this.cosechas);
			if(!optional.isPresent()){
				return;
			}else{
				cosechasAux[0] =optional.get();
			}
		}

		Task<AmountVsElevacionChart> pfMapTask = new Task<AmountVsElevacionChart>(){
			@Override
			protected AmountVsElevacionChart call() throws Exception {
				try{
					//	Labor labor = optional.get();		
					AmountVsElevacionChart histoChart = new AmountVsElevacionChart(cosechasAux[0],grupos);
					return histoChart;
				}catch(Throwable t){
					t.printStackTrace();
					System.out.println("no hay ninguna labor para mostrar"); //$NON-NLS-1$
					System.out.print(t.getMessage());
					return null;
				}
			}			
		};


		pfMapTask.setOnSucceeded(handler -> {
			AmountVsElevacionChart	histoChart = (AmountVsElevacionChart) handler.getSource().getValue();	
			if(histoChart!=null){
				Stage histoStage = new Stage();
				histoStage.setTitle(Messages.getString("JFXMain.heightVsAmountChartTitle")); //$NON-NLS-1$
				histoStage.getIcons().add(new Image(ICON));

				Scene scene = new Scene(histoChart, 800,450);
				histoStage.setScene(scene);
				System.out.println("termine de crear el grafico rinde vs altura"); //$NON-NLS-1$
				histoStage.initOwner(JFXMain.stage);
				histoStage.show();
				System.out.println("histoChart.show();"); //$NON-NLS-1$
			}else{
				Alert error = new Alert(AlertType.ERROR);
				error.setTitle(Messages.getString("JFXMain.heightVsAmountErrorTitle")); //$NON-NLS-1$
				error.setContentText(Messages.getString("JFXMain.heightVsAmountErrorText")); //$NON-NLS-1$
				error.show();
			}
		});
		executorPool.execute(pfMapTask);

		//		Thread currentTaskThread = new Thread(pfMapTask);
		//		currentTaskThread.setDaemon(true);
		//		currentTaskThread.start();
	}
	
	/**
	 * metodo que muestra una tabla con poligonos que se pueden seleccionar para descargar el valor de los ndvi
	 * de cada uno dentro de un periodo determinado
	 */
	private void doBulkNDVIDownload() {
	 BulkNdviDownloadGUI gui = new BulkNdviDownloadGUI();
	 gui.show();
	}


	private void showHistoNDVI(Ndvi  ndvi) {	

		Task<Parent> pfMapTask = new Task<Parent>(){
			@Override
			protected Parent call() throws Exception {
				try{	
					NDVIHistoChart histoChart = new NDVIHistoChart(ndvi);
					return histoChart;
				}catch(Throwable t){
					t.printStackTrace();
					System.out.println("no hay ninguna ndvi para mostrar"); //$NON-NLS-1$
					return new VBox(new Label(Messages.getString("JFXMain.207"))); //$NON-NLS-1$
				}
			}			
		};


		pfMapTask.setOnSucceeded(handler -> {
			Parent	histoChart = (Parent) handler.getSource().getValue();	
			Stage histoStage = new Stage();
			//	histoStage.setTitle("Histograma Cosecha");
			histoStage.getIcons().add(new Image(ICON));

			Scene scene = new Scene(histoChart, 800,450);
			histoStage.setScene(scene);
			//	System.out.println("termine de crear el histo chart");
			histoStage.initOwner(JFXMain.stage);
			histoStage.show();
			//	System.out.println("histoChart.show();");
		});
		executorPool.submit(pfMapTask);
	}

	private void showHistoLabor(Labor<?> cosechaLabor) {	
		Platform.runLater(()->{
			CosechaHistoChart histoChart = new CosechaHistoChart(cosechaLabor);
			Stage histoStage = new Stage();
			histoStage.getIcons().add(new Image(ICON));
			Scene scene = new Scene(histoChart, 800,450);
			histoStage.setScene(scene);
			histoStage.initOwner(JFXMain.stage);
			histoStage.show();
		});
	}


//	private LocalDate dateChooser(LocalDate ini){
//		SimpleObjectProperty<LocalDate> ldp = new SimpleObjectProperty<LocalDate>();
//		LocalDate initialDate = LocalDate.now();
//		DateConverter dc = new DateConverter();
//		Configuracion config = Configuracion.getInstance();
//		String configDate = config.getPropertyOrDefault(Messages.getString("JFXMain.208"), dc.toString(initialDate)); //$NON-NLS-1$
//		initialDate = dc.fromString(configDate);
//		if(ini!=null){
//			initialDate= ini;
//		}
//		ldp.set(initialDate);
//
//		Alert dateDialog = new Alert(AlertType.CONFIRMATION);//dc.toString(initialDate));
//		DatePicker datePickerFecha=new DatePicker();
//		datePickerFecha.setConverter(new DateConverter());
//		datePickerFecha.valueProperty().bindBidirectional(ldp);
//
//		dateDialog.setGraphic(datePickerFecha);
//		dateDialog.setTitle(Messages.getString("JFXMain.209")); //$NON-NLS-1$
//		dateDialog.setHeaderText(Messages.getString("JFXMain.210")); //$NON-NLS-1$
//		dateDialog.initOwner(this.stage);
//		Optional<ButtonType> res = dateDialog.showAndWait();
//		if(res.get().equals(ButtonType.OK)){
//			config.setProperty(Messages.getString("JFXMain.211"), dc.toString(ldp.get())); //$NON-NLS-1$
//			config.save();
//			return ldp.get();
//		} else {
//			return null;
//		}
//	}

	//	private void doGetNdviTiffFile(Object labor) {
	//		LocalDate ini =null;
	//		if(labor instanceof Labor){
	//			ini= (LocalDate)((Labor<?>)labor).fechaProperty.getValue();
	//		} 
	//		 ini = dateChooser(ini);
	//		
	//		if(ini!=null){
	//			File downloadLocation = directoryChooser();
	//			if(downloadLocation==null)return;
	//			ObservableList<File> observableList = FXCollections.observableArrayList(new ArrayList<File>());
	//			observableList.addListener((ListChangeListener<File>) c -> {
	//				System.out.println("mostrando los archivos agregados");
	//				if(c.next()){
	//					c.getAddedSubList().forEach((file)->{
	//						showNdviTiffFile(file);
	//					});//fin del foreach
	//				}			
	//			});
	//
	//			GetNDVIForLaborTask task = new GetNDVIForLaborTask(labor,downloadLocation,observableList);
	//			task.setDate(ini);
	//			task.installProgressBar(progressBox);
	//			task.setOnSucceeded(handler -> {
	//				if(labor instanceof Poligono){
	//					((Poligono)labor).getLayer().setEnabled(false);
	//				}
	//				task.uninstallProgressBar();
	//			});
	//			executorPool.submit(task);
	//		}
	//	}

	/**
	 * descargar los tiff correspondientes a un polygono y mostrarlos como ndvi
	 * @param placementObject
	 */
	private void doGetNdviTiffFile(Object placementObject) {//ndvi2
		LocalDate fin =null;
		if(placementObject !=null && Labor.class.isAssignableFrom(placementObject.getClass())){
			fin= DateConverter.asLocalDate((Date)((Labor<?>)placementObject).getFecha());
		} 
		//fin = dateChooser(fin);
		NdviDatePickerDialog ndviDpDLG = new NdviDatePickerDialog(JFXMain.stage);
		LocalDate ret = ndviDpDLG.ndviDateChooser(fin);
		if(ret ==null)return;//seleccionar fecha termino en cancel.
		//System.out.println(Messages.getString("JFXMain.212")+ndviDpDLG.initialDate+Messages.getString("JFXMain.213")+ndviDpDLG.finalDate); //$NON-NLS-1$ //$NON-NLS-2$
		//Begin: 2018-02-28 End: 2018-03-28
		fin = ndviDpDLG.finalDate;


		if(fin!=null){
			File downloadLocation=null;
			try {
				downloadLocation = File.createTempFile(Messages.getString("JFXMain.214"), Messages.getString("JFXMain.215")).getParentFile(); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {

				e.printStackTrace();
			}//directoryChooser();
			if(downloadLocation==null)return;
			ObservableList<Ndvi> observableList = FXCollections.observableArrayList(new ArrayList<Ndvi>());
			observableList.addListener((ListChangeListener<Ndvi>) c -> {
				System.out.println(Messages.getString("JFXMain.216")); //$NON-NLS-1$
				if(c.next()){
					c.getAddedSubList().forEach((ndvi)->{
						showNdviTiffFile(ndvi.getF(), placementObject,ndvi);
					});//fin del foreach
				}			
			});

			GetNdviForLaborTask3 task = new GetNdviForLaborTask3(placementObject,downloadLocation,observableList);
			task.setFinDate(fin);
			task.setBeginDate(ndviDpDLG.initialDate);
			task.installProgressBar(progressBox);
			task.setOnSucceeded(handler -> {
				if(placementObject instanceof Poligono){
					((Poligono)placementObject).getLayer().setEnabled(false);
				}
				task.uninstallProgressBar();
			});
			System.out.println(Messages.getString("JFXMain.217")); //$NON-NLS-1$
			executorPool.submit(task);
		}
	}

	/**
	 * tomar un Ndvi y mostrarlo como layer
	 * @param ndvi
	 */
	public void doShowNDVI(Ndvi ndvi) {
		executorPool.submit(()->{
			ndvi.loadFileFromContent();
			showNdviTiffFile(ndvi.getF(),null,ndvi);

		});

	}

	/**
	 * seleccionar archivos .tif y mostrarlos como Ndvi
	 */
	private void doOpenNDVITiffFiles() {
		List<File>	files =FileHelper.chooseFiles("TIF","*.tif"); //$NON-NLS-1$ //$NON-NLS-2$
		if(files!=null)	files.forEach((file)->{
			showNdviTiffFile(file,null,null);
		});//fin del foreach
	}

	private void showNdviTiffFile(File file, Object placementObject,Ndvi _ndvi) {
		ShowNDVITifFileTask task = new ShowNDVITifFileTask(file,_ndvi);
		if( placementObject!=null && Poligono.class.isAssignableFrom(placementObject.getClass())){
			task.setPoligono((Poligono) placementObject);
		}
		task.setOnSucceeded(handler -> {
			Layer ndviLayer = (Layer) handler.getSource().getValue();	
			insertBeforeCompass(getWwd(), ndviLayer);
			this.getLayerPanel().update(this.getWwd());
			viewGoTo(ndviLayer);
			playSound();	
		});
		executorPool.execute(task);

	}

	//	private boolean isLocalPath(String path){
	//		return new File(path).exists();
	//	}


	protected void importImagery()  {
		List<File>	files =FileHelper.chooseFiles("TIF","*.tif"); //$NON-NLS-1$ //$NON-NLS-2$
		files.forEach((file)->{
			executorPool.execute(()->{	
				try {
					// Read the data and save it in a temp file.
					File sourceFile = file;

					// Create a raster reader to read this type of file. The reader is created from the currently
					// configured factory. The factory class is specified in the Configuration, and a different one can be
					// specified there.
					DataRasterReaderFactory readerFactory
					= (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
							AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
					DataRasterReader reader = readerFactory.findReaderFor(sourceFile, null);

					// Before reading the raster, verify that the file contains imagery.
					AVList metadata = reader.readMetadata(sourceFile, null);
					if (metadata == null || !AVKey.IMAGE.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
						throw new Exception(Messages.getString("JFXMain.222")); //$NON-NLS-1$

					// Read the file into the raster. read() returns potentially several rasters if there are multiple
					// files, but in this case there is only one so just use the first element of the returned array.
					DataRaster[] rasters = reader.read(sourceFile, null);
					if (rasters == null || rasters.length == 0)
						throw new Exception(Messages.getString("JFXMain.223")); //$NON-NLS-1$

					DataRaster raster = rasters[0];

					// Determine the sector covered by the image. This information is in the GeoTIFF file or auxiliary
					// files associated with the image file.
					final Sector sector = (Sector) raster.getValue(AVKey.SECTOR);
					if (sector == null)
						throw new Exception(Messages.getString("JFXMain.224")); //$NON-NLS-1$

					// Request a sub-raster that contains the whole image. This step is necessary because only sub-rasters
					// are reprojected (if necessary); primary rasters are not.
					int width = raster.getWidth();
					int height = raster.getHeight();


					// getSubRaster() returns a sub-raster of the size specified by width and height for the area indicated
					// by a sector. The width, height and sector need not be the full width, height and sector of the data,
					// but we use the full values of those here because we know the full size isn't huge. If it were huge
					// it would be best to get only sub-regions as needed or install it as a tiled image layer rather than
					// merely import it.
					DataRaster subRaster = raster.getSubRaster(width, height, sector, null);

					// Tne primary raster can be disposed now that we have a sub-raster. Disposal won't affect the
					// sub-raster.
					raster.dispose();

					// Verify that the sub-raster can create a BufferedImage, then create one.
					if (!(subRaster instanceof BufferedImageRaster))
						throw new Exception(Messages.getString("JFXMain.225")); //$NON-NLS-1$
					BufferedImage image = ((BufferedImageRaster) subRaster).getBufferedImage();


					DataBuffer buffer = image.getData().getDataBuffer();


					double [][] doubleArray = new double[width*height][3];
					for(int x=0;x<width;x++){
						for(int y=0;y<height;y++){
							int index = y*width+x;
							double r = buffer.getElemDouble(0,index);
							double g = buffer.getElemDouble(1,index);
							double v = buffer.getElemDouble(2,index);
							doubleArray[index][0]=r;
							doubleArray[index][1]=g;
							doubleArray[index][2]=v;

						}
					}
					// BarnesHutTSne  tsne = new ParallelBHTsne();
					//  double [][] Y = tsne.tsne(doubleArray, 1, 3, 20);   
					// System.out.println(MatrixOps.doubleArrayToPrintString(Y, ", ", 50,10));

					// The sub-raster can now be disposed. Disposal won't affect the BufferedImage.
					subRaster.dispose();


					// Create a SurfaceImage to display the image over the specified sector.
					final SurfaceImage si1 = new SurfaceImage(image, sector);

					// On the event-dispatch thread, add the imported data as an SurfaceImageLayer.
					Platform.runLater(()->{
						// Add the SurfaceImage to a layer.
						SurfaceImageLayer layer = new SurfaceImageLayer();
						layer.setName(Messages.getString("JFXMain.226")); //$NON-NLS-1$
						layer.setPickEnabled(false);
						layer.addRenderable(si1);

						// Add the layer to the model and update the application's layer panel.
						insertBeforeCompass(getWwd(), layer);
						this.getLayerPanel().update(this.getWwd());


						// Set the view to look at the imported image.
						ExampleUtil.goTo(getWwd(), sector);

					});
				}
				catch (Exception e){
					e.printStackTrace();
				}
			});//execute
		});//foreach
	}

	//	private BufferWrapperRaster loadRasterFile(File file){
	//		if(!file.exists()){	
	//			// si el recurso es web podemos bajarlo a 
	//			// Download the data and save it in a temp file.
	//			String path = file.getAbsolutePath();
	//			file = ExampleUtil.saveResourceToTempFile(path, "." + WWIO.getSuffix(path));
	//		}
	//
	//
	//
	//		// Create a raster reader for the file type.
	//		DataRasterReaderFactory readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
	//				AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
	//		DataRasterReader reader = readerFactory.findReaderFor(file, null);
	//
	//		try{
	//			// Before reading the raster, verify that the file contains elevations.
	//			AVList metadata = reader.readMetadata(file, null);
	//			if (metadata == null || !AVKey.ELEVATION.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
	//			{
	//				Platform.runLater(()->{
	//					Alert imagenAlert = new Alert(Alert.AlertType.ERROR);
	//					imagenAlert.initOwner(stage);
	//					imagenAlert.initModality(Modality.NONE);
	//					imagenAlert.setTitle("Archivo no compatible");
	//					imagenAlert.setContentText("El archivo no continen informacion ndvi. Por favor seleccione un archivo con solo una capa");
	//					imagenAlert.show();
	//				});
	//				String msg = Logging.getMessage("ElevationModel.SourceNotElevations", file.getAbsolutePath());
	//				Logging.logger().severe(msg);
	//				throw new IllegalArgumentException(msg);
	//			}
	//
	//			// Read the file into the raster.
	//			DataRaster[] rasters = reader.read(file, null);
	//			if (rasters == null || rasters.length == 0)	{
	//				String msg = Logging.getMessage("ElevationModel.CannotReadElevations", file.getAbsolutePath());
	//				Logging.logger().severe(msg);
	//				throw new WWRuntimeException(msg);
	//			}
	//
	//			// Determine the sector covered by the elevations. This information is in the GeoTIFF file or auxiliary
	//			// files associated with the elevations file.
	//			Sector sector = (Sector) rasters[0].getValue(AVKey.SECTOR);
	//			if (sector == null)
	//			{
	//				String msg = Logging.getMessage("DataRaster.MissingMetadata", AVKey.SECTOR);
	//				Logging.logger().severe(msg);
	//				throw new IllegalArgumentException(msg);
	//			}
	//
	//			// Request a sub-raster that contains the whole file. This step is necessary because only sub-rasters
	//			// are reprojected (if necessary); primary rasters are not.
	//			int width = rasters[0].getWidth();
	//			int height = rasters[0].getHeight();
	//
	//			DataRaster subRaster = rasters[0].getSubRaster(width, height, sector, rasters[0]);
	//
	//			// Verify that the sub-raster can create a ByteBuffer, then create one.
	//			if (!(subRaster instanceof BufferWrapperRaster))
	//			{
	//				String msg = Logging.getMessage("ElevationModel.CannotCreateElevationBuffer", file.getName());
	//				Logging.logger().severe(msg);
	//				throw new WWRuntimeException(msg);
	//			}
	//
	//			return (BufferWrapperRaster) subRaster;
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//			return null;
	//		}
	//	}

	private void doCrearSuelo(Poligono poli) {
		Suelo labor = new Suelo();
		labor.setNombre(poli.getNombre());
		
		LaborLayer layer = new LaborLayer();
		labor.setLayer(layer);

		//		Optional<Suelo> cosechaConfigured= SueloConfigDialogController.config(labor);
		//		if(!cosechaConfigured.isPresent()){//
		//			System.out.println("el dialogo termino con cancel asi que no continuo con la cosecha");
		//			labor.dispose();//libero los recursos reservados
		//			return;
		//		}							

		TextInputDialog ppmPDialog = new TextInputDialog(Messages.getString("JFXMain.228")); //$NON-NLS-1$
		ppmPDialog.setTitle(Messages.getString("JFXMain.229")); //$NON-NLS-1$
		ppmPDialog.setContentText(Messages.getString("JFXMain.230")); //$NON-NLS-1$
		Optional<String> ppmPOptional = ppmPDialog.showAndWait();
		Double ppmP = Double.valueOf(ppmPOptional.get());

		TextInputDialog ppmNDialog = new TextInputDialog(Messages.getString("JFXMain.231")); //$NON-NLS-1$
		ppmNDialog.setTitle(Messages.getString("JFXMain.232")); //$NON-NLS-1$
		ppmNDialog.setContentText(Messages.getString("JFXMain.233")); //$NON-NLS-1$
		Optional<String> ppmNOptional = ppmNDialog.showAndWait();
		Double ppmN = Double.valueOf(ppmNOptional.get());

		TextInputDialog pMODialog = new TextInputDialog(Messages.getString("JFXMain.234")); //$NON-NLS-1$
		pMODialog.setTitle(Messages.getString("JFXMain.235")); //$NON-NLS-1$
		pMODialog.setContentText(Messages.getString("JFXMain.236")); //$NON-NLS-1$
		Optional<String> pMOOptional = pMODialog.showAndWait();
		Double pMO = Double.valueOf(pMOOptional.get());


		CrearSueloMapTask umTask = new CrearSueloMapTask(labor,poli,ppmP,ppmN,pMO);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			Suelo ret = (Suelo)handler.getSource().getValue();
			suelos.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			umTask.uninstallProgressBar();
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.237")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}

	/**
	 * metodo que toma los poligonos de la labor y genera un mapa de puntos con las densidades configuradas
	 * preguntar si desea generar el muestreo por cantidad de muestras por poligono o densidad de muestras por poligono
	 * permitir configurar cantidad max y min de muestras
	 * permitir configurar superficie minima relevante
	 * @param l una Labor
	 */
	private void doGenerarMuestreoDirigido(Labor<?> l) {		 

		double superficieMinimaAMuestrear=0;
		double densidadDeMuestrasDeseada=0;
		double cantidadMinimaDeMuestrasPoligonoAMuestrear=0;

		TextInputDialog supMinDialog = new TextInputDialog(Messages.getString("JFXMain.238")); //$NON-NLS-1$
		supMinDialog.setTitle(Messages.getString("JFXMain.239")); //$NON-NLS-1$
		supMinDialog.setContentText(Messages.getString("JFXMain.240")); //$NON-NLS-1$
		Optional<String> ppmPOptional = supMinDialog.showAndWait();
		superficieMinimaAMuestrear = Double.valueOf(ppmPOptional.get());

		TextInputDialog densidadDialog = new TextInputDialog(Messages.getString("JFXMain.241")); //$NON-NLS-1$
		densidadDialog.setTitle(Messages.getString("JFXMain.242")); //$NON-NLS-1$
		densidadDialog.setContentText(Messages.getString("JFXMain.243")); //$NON-NLS-1$
		Optional<String> densidadOptional = densidadDialog.showAndWait();
		densidadDeMuestrasDeseada = Double.valueOf(densidadOptional.get());

		TextInputDialog ppmNDialog = new TextInputDialog(Messages.getString("JFXMain.244")); //$NON-NLS-1$
		ppmNDialog.setTitle(Messages.getString("JFXMain.245")); //$NON-NLS-1$
		ppmNDialog.setContentText(Messages.getString("JFXMain.246")); //$NON-NLS-1$
		Optional<String> cantOptional = ppmNDialog.showAndWait();
		cantidadMinimaDeMuestrasPoligonoAMuestrear = Double.valueOf(cantOptional.get());

		GenerarMuestreoDirigidoTask umTask = new GenerarMuestreoDirigidoTask(Collections.singletonList(l),superficieMinimaAMuestrear,densidadDeMuestrasDeseada,cantidadMinimaDeMuestrasPoligonoAMuestrear);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			Suelo ret = (Suelo)handler.getSource().getValue();
			System.out.println(Messages.getString("JFXMain.247")); //$NON-NLS-1$
			suelos.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			umTask.uninstallProgressBar();
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.248")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}

	private void doCrearPulverizacion(Poligono poli) {
		PulverizacionLabor labor = new PulverizacionLabor();
		labor.setNombre(poli.getNombre());
		labor.setNombre(poli.getNombre()+" "+Messages.getString("JFXMain.pulverizacion")); //$NON-NLS-1$ //$NON-NLS-2$
		LaborLayer layer = new LaborLayer();
		labor.setLayer(layer);
		Optional<PulverizacionLabor> cosechaConfigured= PulverizacionConfigDialogController.config(labor);
		if(!cosechaConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.249")); //$NON-NLS-1$
			labor.dispose();//libero los recursos reservados
			return;
		}							

		TextInputDialog anchoDialog = new TextInputDialog(Messages.getString("JFXMain.250")); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.251")); //$NON-NLS-1$
		anchoDialog.setContentText(Messages.getString("JFXMain.252")); //$NON-NLS-1$
		Optional<String> anchoOptional = anchoDialog.showAndWait();
		Double rinde = Double.valueOf(anchoOptional.get());
		CrearPulverizacionMapTask umTask = new CrearPulverizacionMapTask(labor,poli,rinde);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			PulverizacionLabor ret = (PulverizacionLabor)handler.getSource().getValue();
			pulverizaciones.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			poli.getLayer().setEnabled(false);
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.253")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}


	private void doCrearSiembra(Poligono poli) {
		SiembraLabor labor = new SiembraLabor();
		LaborLayer layer = new LaborLayer();
		labor.setLayer(layer);
		labor.setNombre(poli.getNombre()+" "+Messages.getString("JFXMain.255")); //$NON-NLS-1$ //$NON-NLS-2$
		Optional<SiembraLabor> siembraConfigured= SiembraConfigDialogController.config(labor);
		if(!siembraConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.256")); //$NON-NLS-1$
			labor.dispose();//libero los recursos reservados
			return;
		}							
		//TODO modificar el dialogo para permitir ingresar la cantidad y la unidad incluyendo kg/ha plantas/m2 y miles de plantas/Ha
		TextInputDialog anchoDialog = new TextInputDialog(Messages.getString("JFXMain.257")); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.258")); //$NON-NLS-1$
		anchoDialog.setContentText(Messages.getString("JFXMain.259")); //$NON-NLS-1$
		Optional<String> anchoOptional = anchoDialog.showAndWait();
		Double rinde = Double.valueOf(anchoOptional.get());
		CrearSiembraMapTask umTask = new CrearSiembraMapTask(labor,poli,rinde);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			SiembraLabor ret = (SiembraLabor)handler.getSource().getValue();
			siembras.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			poli.getLayer().setEnabled(false);
			umTask.uninstallProgressBar();
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.260")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}

	private void doCrearFertilizacion(Poligono poli) {
		FertilizacionLabor labor = new FertilizacionLabor();
		
		labor.setNombre(poli.getNombre()+" "+Messages.getString("JFXMain.fertilizacion")); //$NON-NLS-1$ //$NON-NLS-2$
		LaborLayer layer = new LaborLayer();
		labor.setLayer(layer);
		Optional<FertilizacionLabor> cosechaConfigured= FertilizacionConfigDialogController.config(labor);
		if(!cosechaConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.261")); //$NON-NLS-1$
			labor.dispose();//libero los recursos reservados
			return;
		}							

		TextInputDialog anchoDialog = new TextInputDialog(Messages.getString("JFXMain.262")); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.263")); //$NON-NLS-1$
		anchoDialog.setContentText(Messages.getString("JFXMain.264")); //$NON-NLS-1$
		Optional<String> anchoOptional = anchoDialog.showAndWait();
		Double rinde = Double.valueOf(anchoOptional.get());
		
		CrearFertilizacionMapTask umTask = new CrearFertilizacionMapTask(labor,poli,rinde);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			FertilizacionLabor ret = (FertilizacionLabor)handler.getSource().getValue();
			fertilizaciones.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			poli.getLayer().setEnabled(false);
			umTask.uninstallProgressBar();
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.265")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}

	private void doCrearCosecha(Poligono poli) {
		CosechaLabor labor = new CosechaLabor();
		LaborLayer layer = new LaborLayer();
		labor.setLayer(layer);
		labor.setNombre(poli.getNombre());
		Optional<CosechaLabor> cosechaConfigured= HarvestConfigDialogController.config(labor);
		if(!cosechaConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.266")); //$NON-NLS-1$
			labor.dispose();//libero los recursos reservados
			return;
		}							

		TextInputDialog anchoDialog = new TextInputDialog(Messages.getString("JFXMain.267")); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.268")); //$NON-NLS-1$
		anchoDialog.setContentText(Messages.getString("JFXMain.269")); //$NON-NLS-1$
		Optional<String> anchoOptional = anchoDialog.showAndWait();
		Double rinde = Double.valueOf(anchoOptional.get());
		CrearCosechaMapTask umTask = new CrearCosechaMapTask(labor,poli,rinde);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
			cosechas.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			poli.getLayer().setEnabled(false);
			umTask.uninstallProgressBar();
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.270")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}



	private void doConvertirNdviACosecha(Ndvi ndvi) {
		CosechaLabor labor = new CosechaLabor();
		labor.setNombre(ndvi.getNombre());
		labor.getConfiguracion().correccionFlowToRindeProperty().setValue(false);
		LaborLayer layer = new LaborLayer();
		labor.setLayer(layer);
		Optional<CosechaLabor> cosechaConfigured= HarvestConfigDialogController.config(labor);
		if(!cosechaConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.271")); //$NON-NLS-1$
			labor.dispose();//libero los recursos reservados
			return;
		}							

		Double rinde = null;
		try {
			TextInputDialog anchoDialog = new TextInputDialog(Messages.getString("JFXMain.272")); //$NON-NLS-1$
			anchoDialog.setTitle(Messages.getString("JFXMain.273")); //$NON-NLS-1$
			anchoDialog.setContentText(Messages.getString("JFXMain.274")); //$NON-NLS-1$
			anchoDialog.initOwner(JFXMain.stage);
			Optional<String> anchoOptional = anchoDialog.showAndWait();
			rinde = Double.valueOf(anchoOptional.get());
		}catch(java.lang.NumberFormatException e) {

			DecimalFormat format=(DecimalFormat) DecimalFormat.getInstance();
			DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
			char sep=symbols.getDecimalSeparator();

			Alert a = new Alert(Alert.AlertType.ERROR);
			a.initOwner(JFXMain.stage);

			a.setTitle(Messages.getString("JFXMain.275")); //$NON-NLS-1$
			a.setHeaderText(Messages.getString("JFXMain.276")+sep+Messages.getString("JFXMain.277")); //$NON-NLS-1$ //$NON-NLS-2$
			a.setContentText(e.getMessage());
			a.show();
			return;
		}

		ConvertirNdviACosechaTask umTask = new ConvertirNdviACosechaTask(labor,ndvi,rinde);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
			cosechas.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			umTask.uninstallProgressBar();
			viewGoTo(ret);
			umTask.uninstallProgressBar();
			System.out.println(Messages.getString("JFXMain.278")); //$NON-NLS-1$
			playSound();
			ndvi.getLayer().setEnabled(false);
			//ndvi.getSurfaceLayer().setVisible(false);
		});//fin del OnSucceeded
		JFXMain.executorPool.execute(umTask);		
	}

	private void doEditCosecha(CosechaLabor cConfigured ) {
		if(cConfigured==null){
			Optional<CosechaLabor> cosechaSelected = HarvestSelectDialogController.select(this.cosechas);
			if(cosechaSelected.isPresent()){
				cConfigured= cosechaSelected.get();
			} else {
				return;
			}
		}

		Optional<CosechaLabor> cosechaConfigured=HarvestConfigDialogController.config(cConfigured);
		if(cosechaConfigured.isPresent()){
			cConfigured = cosechaConfigured.get();
			ProcessHarvestMapTask umTask = new ProcessHarvestMapTask(cConfigured);
			umTask.installProgressBar(progressBox);
			umTask.setOnSucceeded(handler -> {
				this.getLayerPanel().update(this.getWwd());
				umTask.uninstallProgressBar();

				this.wwjPanel.repaint();
				System.out.println(Messages.getString("JFXMain.279")); //$NON-NLS-1$
				playSound();
			});//fin del OnSucceeded						
			JFXMain.executorPool.execute(umTask);
		}
	}

	private void doExtraerPoligonos(Labor<?> labor ) {	
		ExtraerPoligonosDeLaborTask umTask = new ExtraerPoligonosDeLaborTask(labor);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {

			@SuppressWarnings("unchecked")
			List<Poligono> poligonos = (List<Poligono>)handler.getSource().getValue();
			this.showPoligonos(poligonos);
			umTask.uninstallProgressBar();

			this.wwjPanel.repaint();
			System.out.println(Messages.getString("JFXMain.280")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded						
		//umTask.start();
		JFXMain.executorPool.execute(umTask);

	}

	private void doEditSuelo(Suelo cConfigured) {			
		Optional<Suelo> cosechaConfigured= SueloConfigDialogController.config(cConfigured);
		if(cosechaConfigured.isPresent()){
			cConfigured = cosechaConfigured.get();
			OpenSoilMapTask umTask = new OpenSoilMapTask(cConfigured);
			umTask.installProgressBar(progressBox);

			umTask.setOnSucceeded(handler -> {
				//Suelo ret = (Suelo)handler.getSource().getValue();


				this.getLayerPanel().update(this.getWwd());
				umTask.uninstallProgressBar();
				this.wwjPanel.repaint();
				playSound();
			});//fin del OnSucceeded
			//umTask.start();
			JFXMain.executorPool.execute(umTask);
		}
	}


	private void doEditPulverizacion(PulverizacionLabor cConfigured ) {
		Optional<PulverizacionLabor> cosechaConfigured=PulverizacionConfigDialogController.config(cConfigured);
		if(cosechaConfigured.isPresent()){
			cConfigured = cosechaConfigured.get();
			//cConfigured.getLayer().removeAllRenderables();
			ProcessPulvMapTask umTask = new ProcessPulvMapTask(cConfigured);
			umTask.installProgressBar(progressBox);

			umTask.setOnSucceeded(handler -> {
				//CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
				this.getLayerPanel().update(this.getWwd());
				umTask.uninstallProgressBar();
				//	viewGoTo(ret);
				this.wwjPanel.repaint();
				System.out.println(Messages.getString("JFXMain.281")); //$NON-NLS-1$
				playSound();
			});//fin del OnSucceeded						
			//umTask.start();
			JFXMain.executorPool.execute(umTask);
		}
	}

	private void doEditSiembra(SiembraLabor cConfigured ) {
		Optional<SiembraLabor> cosechaConfigured=SiembraConfigDialogController.config(cConfigured);
		if(cosechaConfigured.isPresent()){
			cConfigured = cosechaConfigured.get();
			//cConfigured.getLayer().removeAllRenderables();
			ProcessSiembraMapTask umTask = new ProcessSiembraMapTask(cConfigured);
			umTask.installProgressBar(progressBox);

			umTask.setOnSucceeded(handler -> {
				this.getLayerPanel().update(this.getWwd());
				umTask.uninstallProgressBar();
				this.wwjPanel.repaint();
				System.out.println(Messages.getString("JFXMain.282")); //$NON-NLS-1$
				playSound();
			});//fin del OnSucceeded						
			//umTask.start();
			JFXMain.executorPool.execute(umTask);
		}
	}

	private void doEditFertilizacion(FertilizacionLabor cConfigured ) {
		Optional<FertilizacionLabor> cosechaConfigured=FertilizacionConfigDialogController.config(cConfigured);
		if(cosechaConfigured.isPresent()){
			cConfigured = cosechaConfigured.get();
			ProcessFertMapTask umTask = new ProcessFertMapTask(cConfigured);
			umTask.installProgressBar(progressBox);

			umTask.setOnSucceeded(handler -> {
				//CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
				this.getLayerPanel().update(this.getWwd());
				umTask.uninstallProgressBar();
				//	viewGoTo(ret);
				this.wwjPanel.repaint();
				System.out.println(Messages.getString("JFXMain.283")); //$NON-NLS-1$
				playSound();
			});//fin del OnSucceeded						
			//umTask.start();
			JFXMain.executorPool.execute(umTask);
		}
	}
	
	// junta 2 o mas cosechas en una 
		private void doUnirSiembras(SiembraLabor siembraLabor) {
			List<SiembraLabor> siemrbasAUnir = new ArrayList<SiembraLabor>();
			if(siembraLabor == null){
				List<SiembraLabor> cosechasEnabled = siembras.stream().filter((l)->{
					Layer layer =l.getLayer();
					return layer!=null && layer.isEnabled();}).collect(Collectors.toList());
				siemrbasAUnir.addAll( cosechasEnabled);//si no hago esto me da un concurrent modification exception al modificar layers en paralelo
			} else {
				siemrbasAUnir.add(siembraLabor);

			}
		

			UnirSiembrasMapTask umTask = new UnirSiembrasMapTask(siemrbasAUnir);
			
			umTask.installProgressBar(progressBox);

			umTask.setOnSucceeded(handler -> {
				SiembraLabor ret = (SiembraLabor)handler.getSource().getValue();
				if(ret.getLayer()!=null){
					siembras.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
				}
				umTask.uninstallProgressBar();
				viewGoTo(ret);

				System.out.println(Messages.getString("JFXMain.286")); //$NON-NLS-1$
				playSound();
			});//fin del OnSucceeded						
			//umTask.start();					
			JFXMain.executorPool.execute(umTask);
			//			}
			//		}
		}
		
		
	private void doCortarCosecha(CosechaLabor cosechaAcortar) {
		
		List<Poligono> geometriasActivas = new ArrayList<Poligono>();
		//1 obtener los poligonos activos
		LayerList layers = this.getWwd().getModel().getLayers();
		for (Layer l : layers) {
			Object o = l.getValue(Labor.LABOR_LAYER_IDENTIFICATOR);
			if (l.isEnabled() && o instanceof Poligono){
				Poligono p = (Poligono)o;
				geometriasActivas.add(p);
			}
		}
		CortarCosechaMapTask umTask = new CortarCosechaMapTask(cosechaAcortar,geometriasActivas);
		
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
			if(ret.getLayer()!=null){
				cosechas.add(ret);
				insertBeforeCompass(getWwd(), ret.getLayer());
				this.getLayerPanel().update(this.getWwd());
			}
			umTask.uninstallProgressBar();
			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.286")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded						
		//umTask.start();					
		JFXMain.executorPool.execute(umTask);
		
	}

	// junta 2 o mas cosechas en una 
	private void doUnirCosechas(CosechaLabor cosechaLabor) {
		List<CosechaLabor> cosechasAUnir = new ArrayList<CosechaLabor>();
		if(cosechaLabor == null){
			List<CosechaLabor> cosechasEnabled = cosechas.stream().filter((l)->{
				Layer layer =l.getLayer();
				return layer!=null&&layer.isEnabled();}).collect(Collectors.toList());
			cosechasAUnir.addAll( cosechasEnabled);//si no hago esto me da un concurrent modification exception al modificar layers en paralelo
		} else {

			cosechasAUnir.add(cosechaLabor);

		}
		boolean calibrar =false;

		if(cosechasAUnir.size()>1) {
			Alert calibrarAlert = new Alert(Alert.AlertType.CONFIRMATION);
			calibrarAlert.setTitle(Messages.getString("JFXMain.284")); //$NON-NLS-1$
			calibrarAlert.setContentText(Messages.getString("JFXMain.285")); //$NON-NLS-1$

			Optional<ButtonType> calibrarButton = calibrarAlert.showAndWait();
			if(calibrarButton.isPresent()){
				if(calibrarButton.get().equals(ButtonType.OK)){
					calibrar=true;
				}

			} else{
				return;
			}
		}
		UnirCosechasMapTask umTask = new UnirCosechasMapTask(cosechasAUnir);
		umTask.setCalibrar(calibrar);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
			if(ret.getLayer()!=null){
				cosechas.add(ret);
				insertBeforeCompass(getWwd(), ret.getLayer());
				this.getLayerPanel().update(this.getWwd());
			}
			umTask.uninstallProgressBar();
			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.286")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded						
		//umTask.start();					
		JFXMain.executorPool.execute(umTask);
		//			}
		//		}
	}

	private void doUnirFertilizaciones() {
		List<FertilizacionLabor> fertilizacionesAUnir = getFertilizacionesSeleccionadas();//si no hago esto me da un concurrent modification exception al modificar layers en paralelo
	
		UnirFertilizacionesMapTask umTask = new UnirFertilizacionesMapTask(fertilizacionesAUnir);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			FertilizacionLabor ret = (FertilizacionLabor)handler.getSource().getValue();
			if(ret.getLayer()!=null){
				fertilizaciones.add(ret);
				insertBeforeCompass(getWwd(), ret.getLayer());
				this.getLayerPanel().update(this.getWwd());
			}
			umTask.uninstallProgressBar();
			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.287")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded						
		//umTask.start();					
		JFXMain.executorPool.execute(umTask);
		//			}
		//		}
	}

	private void doGrillarCosechas(CosechaLabor cosechaAGrillar) {
		List<CosechaLabor> cosechasAUnir = new ArrayList<CosechaLabor>();
		if(cosechaAGrillar == null){
			List<CosechaLabor> cosechasEnabled = cosechas.stream().filter((l)->
			{
				Layer layer =l.getLayer();
				return layer!=null&&layer.isEnabled();}
					).collect(Collectors.toList());
			cosechasAUnir.addAll( cosechasEnabled);//si no hago esto me da un concurrent modification exception al modificar layers en paralelo
		} else {
			cosechasAUnir.add(cosechaAGrillar);

		}
		Configuracion config = Configuracion.getInstance();
		TextInputDialog anchoDialog = new TextInputDialog(config.getPropertyOrDefault(CosechaConfig.ANCHO_GRILLA_KEY,Messages.getString("JFXMain.288"))); //$NON-NLS-1$
		anchoDialog.setTitle(Messages.getString("JFXMain.289")); //$NON-NLS-1$
		anchoDialog.setContentText(Messages.getString("JFXMain.290")); //$NON-NLS-1$
		Optional<String> anchoOptional = anchoDialog.showAndWait();
		if(anchoOptional.isPresent()){
			//System.out.println("optional is present con valor "+anchoOptional.get());

			config.setProperty(CosechaConfig.ANCHO_GRILLA_KEY,anchoOptional.get());
			config.save();

		} else{
			return;
		}
		
		Alert calibrarAlert = new Alert(Alert.AlertType.CONFIRMATION);
		calibrarAlert.setTitle(Messages.getString("Rellenar Huecos")); //$NON-NLS-1$
		calibrarAlert.setContentText(Messages.getString("Rellenar Huecos")); //$NON-NLS-1$
		boolean rellenarHuecos = false;
		Optional<ButtonType> calibrarButton = calibrarAlert.showAndWait();
		if(calibrarButton.isPresent()){
			if(calibrarButton.get().equals(ButtonType.OK)){
				rellenarHuecos=true;
			}
		}
		
		GrillarCosechasMapTask umTask = new GrillarCosechasMapTask(cosechasAUnir);
		umTask.setRellenarHuecos(rellenarHuecos);
		umTask.installProgressBar(progressBox);

		umTask.setOnSucceeded(handler -> {
			CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
			if(ret.getLayer()!=null){
				cosechas.add(ret);
				insertBeforeCompass(getWwd(), ret.getLayer());
				cosechaAGrillar.getLayer().setEnabled(false);
				this.getLayerPanel().update(this.getWwd());
			}
			umTask.uninstallProgressBar();
			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.291")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded						
		//umTask.start();					
		JFXMain.executorPool.execute(umTask);
	}

	private void doGuardarPoligono(Poligono layerObject){
		layerObject.setActivo(true);
		DAH.save(layerObject);
	}


	private void doGuardarLabor(Labor<?> layerObject) {
		DAH.save(layerObject);		
	}

	// generar un layer de fertilizacion a partir de una cosecha
	//el proceso consiste el levantar las geometrias de la cosecha y preguntarle la usuario
	//que producto aplico y en que densidad por hectarea
	private void doRecomendFertPFromHarvest(CosechaLabor value) {
		FertilizacionLabor labor = new FertilizacionLabor();
		labor.setLayer(new LaborLayer());

		labor.setNombre(value.getNombre()+Messages.getString("JFXMain.292")); //$NON-NLS-1$
		Optional<FertilizacionLabor> cosechaConfigured= FertilizacionConfigDialogController.config(labor);
		if(!cosechaConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.293")); //$NON-NLS-1$
			return;
		}							

		//Dialogo preguntar min y max a aplicar
		Alert minMaxDialog = new Alert(AlertType.CONFIRMATION);
		DecimalFormat df = new DecimalFormat();
		TextField min = new TextField(df.format(0));
		TextField max = new TextField(df.format(0));

		VBox vb = new VBox();
		vb.getChildren().add(new HBox(new Label(Messages.getString("JFXMain.294")),min)); //$NON-NLS-1$
		vb.getChildren().add(new HBox(new Label(Messages.getString("JFXMain.295")),max)); //$NON-NLS-1$

		minMaxDialog.setGraphic(vb);
		minMaxDialog.setTitle(Messages.getString("JFXMain.296")); //$NON-NLS-1$
		minMaxDialog.setContentText(Messages.getString("JFXMain.297")); //$NON-NLS-1$
		//dateDialog.setHeaderText("Fecha Desde");
		minMaxDialog.initOwner(JFXMain.stage);
		Optional<ButtonType> res = minMaxDialog.showAndWait();
		Double minFert =null,maxFert=null; 
		if(res.get().equals(ButtonType.OK)){
			try {
				minFert=df.parse(min.getText()).doubleValue();
				if(minFert==0)minFert=null;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				maxFert=df.parse(max.getText()).doubleValue();
				if(maxFert==0)maxFert=null;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return;
		}

		RecomendFertPFromHarvestMapTask umTask = new RecomendFertPFromHarvestMapTask(labor,value);
		umTask.setMinFert(minFert);
		umTask.setMaxFert(maxFert);
		umTask.installProgressBar(progressBox);

		//	testLayer();
		umTask.setOnSucceeded(handler -> {
			FertilizacionLabor ret = (FertilizacionLabor)handler.getSource().getValue();
			fertilizaciones.add(ret);
			//TODO cambiar esto cuando cambie las acciones a un menu contextual en layerPanel. 
			//no guardar una lista de fertilizaciones sino obtenerlas del arbol.
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			umTask.uninstallProgressBar();

			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.298")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		//	umTask.start();
		JFXMain.executorPool.execute(umTask);
	}

	private void doRecomendFertNFromHarvest(CosechaLabor cosecha) {
		// TODO generar un layer de fertilizacion a partir de una cosecha
		//el proceso consiste el levantar las geometrias de la cosecha y preguntarle la usuario
		//que producto aplico y en que densidad por hectarea

		//TODO permitir generar una aplicacion partida en 2 o mas fechas.

		List<Suelo> suelosEnabled = getSuelosSeleccionados();
		List<FertilizacionLabor> fertEnabled = getFertilizacionesSeleccionadas();



		FertilizacionLabor fertN = new FertilizacionLabor();
		fertN.setLayer(new LaborLayer());

		fertN.setNombre(cosecha.getNombre()+Messages.getString("JFXMain.299")); //$NON-NLS-1$
		Optional<FertilizacionLabor> fertConfigured= FertilizacionConfigDialogController.config(fertN);
		if(!fertConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.300")); //$NON-NLS-1$
			return;
		}							

		//Dialogo preguntar min y max a aplicar
		Alert minMaxDialog = new Alert(AlertType.CONFIRMATION);
		DecimalFormat df = new DecimalFormat();
		TextField min = new TextField(df.format(0));
		TextField max = new TextField(df.format(0));

		VBox vb = new VBox();
		vb.getChildren().add(new HBox(new Label(Messages.getString("JFXMain.301")),min)); //$NON-NLS-1$
		vb.getChildren().add(new HBox(new Label(Messages.getString("JFXMain.302")),max)); //$NON-NLS-1$

		minMaxDialog.setGraphic(vb);
		minMaxDialog.setTitle(Messages.getString("JFXMain.303")); //$NON-NLS-1$
		minMaxDialog.setContentText(Messages.getString("JFXMain.304")); //$NON-NLS-1$
		//dateDialog.setHeaderText("Fecha Desde");
		minMaxDialog.initOwner(JFXMain.stage);
		Optional<ButtonType> res = minMaxDialog.showAndWait();
		Double minFert =null,maxFert=null; 
		if(res.get().equals(ButtonType.OK)){
			try {
				minFert=df.parse(min.getText()).doubleValue();
				if(minFert==0)minFert=null;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				maxFert=df.parse(max.getText()).doubleValue();
				if(maxFert==0)maxFert=null;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return;
		}

		RecomendFertNFromHarvestMapTask umTask = new RecomendFertNFromHarvestMapTask(fertN,cosecha, suelosEnabled,
				fertEnabled);
		umTask.setMinFert(minFert);
		umTask.setMaxFert(maxFert);
		//RecomendFertNFromHarvestMapTask umTask = new RecomendFertNFromHarvestMapTask(labor,value);
		umTask.installProgressBar(progressBox);

		//	testLayer();
		umTask.setOnSucceeded(handler -> {
			FertilizacionLabor ret = (FertilizacionLabor)handler.getSource().getValue();
			fertilizaciones.add(ret);//TODO cambiar esto cuando cambie las acciones a un menu contextual en layerPanel
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());
			umTask.uninstallProgressBar();

			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.305")); //$NON-NLS-1$
			playSound();
		});//fin del OnSucceeded
		//	umTask.start();
		JFXMain.executorPool.execute(umTask);
	}

	private List<Suelo> getSuelosSeleccionados() {
		return suelos.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
	}

	/**
	 * accion ejecutada al presionar el boton openFile Despliega un file
	 * selector e invoca la tarea que muestra el file en pantalla
	 */
	private void doOpenCosecha(List<File> files) {
		List<FileDataStore> stores = FileHelper.chooseShapeFileAndGetMultipleStores(files);
		if (stores != null) {
			for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				CosechaLabor labor = new CosechaLabor(store);
				LaborLayer layer = new LaborLayer();
				labor.setLayer(layer);
				Optional<CosechaLabor> cosechaConfigured= HarvestConfigDialogController.config(labor);
				if(!cosechaConfigured.isPresent()){//
					System.out.println(Messages.getString("JFXMain.306")); //$NON-NLS-1$
					labor.dispose();//libero los recursos reservados
					continue;
				}							

				ProcessHarvestMapTask umTask = new ProcessHarvestMapTask(labor);
				umTask.installProgressBar(progressBox);


				umTask.setOnSucceeded(handler -> {
					CosechaLabor ret = (CosechaLabor)handler.getSource().getValue();
					cosechas.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
					umTask.uninstallProgressBar();
					viewGoTo(ret);
					umTask.uninstallProgressBar();
					System.out.println(Messages.getString("JFXMain.307")); //$NON-NLS-1$
					playSound();
				});//fin del OnSucceeded
				JFXMain.executorPool.execute(umTask);

				//	umTask.start();//crea un nuevo thread y ejecuta el task (ProcessMapTask)
				//Platform.runLater(umTask);
			}//fin del for stores

		}//if stores != null

	}

	/**
	 * accion ejecutada al presionar el boton openFile Despliega un file
	 * selector e invoca la tarea que muestra el file en pantalla
	 */
	private void doOpenFertMap(List<File> files) {
		List<FileDataStore> stores =FileHelper.chooseShapeFileAndGetMultipleStores(files);
		if (stores != null) {
			//	harvestMap.getChildren().clear();
			for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				FertilizacionLabor labor = new FertilizacionLabor(store);
				labor.setLayer(new LaborLayer());
				Optional<FertilizacionLabor> cosechaConfigured= FertilizacionConfigDialogController.config(labor);
				if(!cosechaConfigured.isPresent()){//
					System.out.println(Messages.getString("JFXMain.308")); //$NON-NLS-1$
					continue;
				}							

				ProcessFertMapTask umTask = new ProcessFertMapTask(labor);
				umTask.installProgressBar(progressBox);

				//	testLayer();
				umTask.setOnSucceeded(handler -> {
					FertilizacionLabor ret = (FertilizacionLabor)handler.getSource().getValue();
					fertilizaciones.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
					umTask.uninstallProgressBar();
					viewGoTo(ret);

					System.out.println(Messages.getString("JFXMain.309")); //$NON-NLS-1$
					playSound();
				});//fin del OnSucceeded
				//umTask.start();
				JFXMain.executorPool.execute(umTask);
			}//fin del for stores

		}//if stores != null

	}


	private void doOpenSiembraMap(List<File> files) {
		List<FileDataStore> stores = FileHelper.chooseShapeFileAndGetMultipleStores(files);
		if (stores != null) {
			for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				SiembraLabor labor = new SiembraLabor(store);
				labor.setLayer(new LaborLayer());
				Optional<SiembraLabor> cosechaConfigured= SiembraConfigDialogController.config(labor);
				if(!cosechaConfigured.isPresent()){//
					System.out.println(Messages.getString("JFXMain.310")); //$NON-NLS-1$
					continue;
				}							

				ProcessSiembraMapTask umTask = new ProcessSiembraMapTask(labor);
				umTask.installProgressBar(progressBox);

				umTask.setOnSucceeded(handler -> {
					SiembraLabor ret = (SiembraLabor)handler.getSource().getValue();
					siembras.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
					umTask.uninstallProgressBar();
					viewGoTo(ret);

					System.out.println(Messages.getString("JFXMain.311")); //$NON-NLS-1$
					playSound();
				});//fin del OnSucceeded
				//umTask.start();
				JFXMain.executorPool.execute(umTask);
			}//fin del for stores
		}//if stores != null
	}

/**
 * @see doExtraerPoligonos(Labor<?>) para ver como optimizar este codigo
 * @ metodo que lee todas las geometrias de todos los archivos seleccionados y los carga como poligonos en memoria
 * @param files
 */
	private void doImportarPoligonos(List<File> files) {
		List<FileDataStore> stores = FileHelper.chooseShapeFileAndGetMultipleStores(files);
		executorPool.submit(()->{
			if (stores != null) {for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				System.out.println(Messages.getString("JFXMain.312")); //$NON-NLS-1$
				try {
					String storeName = store.getNames().get(0).getLocalPart().replace("%20", " ");
					//System.out.println(storeName);
					SimpleFeatureSource	source = store.getFeatureSource();

					SimpleFeatureIterator iterator = source.getFeatures().features();
					int i=0;
					while(iterator.hasNext()){
						SimpleFeature feature = iterator.next();	
						
						double has = ProyectionConstants.A_HAS(((Geometry)feature.getDefaultGeometry()).getArea());
						if(has<0.2)continue;//cada poli mayor a 10m2
						Poligono poli = ExtraerPoligonosDeLaborTask.featureToPoligono(feature);
						poli.setNombre(storeName+" ["+Integer.toString(i)+"]");
						i++;
						try {
							//intento tomar el nombre del atributo si tiene la columna Name
							String name = (String) feature.getAttribute("Name");
							if(name!=null) {
								poli.setNombre(name);
							}
							}catch(Exception e) {
								
							}
						
						MeasureTool measureTool = PoligonLayerFactory.createPoligonLayer(poli, this.getWwd(), this.getLayerPanel());
						poli.setArea(has);
						insertBeforeCompass(this.getWwd(), measureTool.getApplicationLayer());
					}//fin del while sobre las features
					this.getLayerPanel().update(this.getWwd());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//fin del for stores
			}//if stores != null
		});
	}

	private void doOpenPulvMap(List<File> files) {
		List<FileDataStore> stores = FileHelper.chooseShapeFileAndGetMultipleStores(files);
		if (stores != null) {
			//	harvestMap.getChildren().clear();
			for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				PulverizacionLabor labor = new PulverizacionLabor(store);
				//	SiembraLabor labor = new SiembraLabor(store);
				labor.setLayer(new LaborLayer());
				Optional<PulverizacionLabor> cosechaConfigured= PulverizacionConfigDialogController.config(labor);
				if(!cosechaConfigured.isPresent()){//
					System.out.println(Messages.getString("JFXMain.313")); //$NON-NLS-1$
					continue;
				}							

				ProcessPulvMapTask umTask = new ProcessPulvMapTask(labor);
				umTask.installProgressBar(progressBox);

				//	testLayer();
				umTask.setOnSucceeded(handler -> {
					PulverizacionLabor ret = (PulverizacionLabor)handler.getSource().getValue();
					pulverizaciones.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
					umTask.uninstallProgressBar();
					viewGoTo(ret);

					System.out.println(Messages.getString("JFXMain.314")); //$NON-NLS-1$
					playSound();
				});//fin del OnSucceeded
				//umTask.start();
				JFXMain.executorPool.execute(umTask);
			}//fin del for stores

		}//if stores != null

	}

	private void doOpenSoilMap(List<File> files) {
		List<FileDataStore> stores = FileHelper.chooseShapeFileAndGetMultipleStores(files);
		if (stores != null) {
			//	harvestMap.getChildren().clear();
			for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				Suelo labor = new Suelo(store);
				//	SiembraLabor labor = new SiembraLabor(store);
				labor.setLayer(new LaborLayer());
				Optional<Suelo> cosechaConfigured= SueloConfigDialogController.config(labor);
				if(!cosechaConfigured.isPresent()){//
					System.out.println(Messages.getString("JFXMain.315")); //$NON-NLS-1$
					continue;
				}							

				OpenSoilMapTask umTask = new OpenSoilMapTask(labor);
				umTask.installProgressBar(progressBox);

				//	testLayer();
				umTask.setOnSucceeded(handler -> {
					Suelo ret = (Suelo)handler.getSource().getValue();
					suelos.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
					umTask.uninstallProgressBar();
					viewGoTo(ret);

					System.out.println(Messages.getString("JFXMain.316")); //$NON-NLS-1$
					playSound();
				});//fin del OnSucceeded
				//umTask.start();
				JFXMain.executorPool.execute(umTask);
			}//fin del for stores
		}//if stores != null
	}

	private void doOpenMarginMap() {
		List<FileDataStore> stores = FileHelper.chooseShapeFileAndGetMultipleStores(null);
		if (stores != null) {
			//	harvestMap.getChildren().clear();
			for(FileDataStore store : stores){//abro cada store y lo dibujo en el harvestMap individualmente
				Margen labor = new Margen(store);
				//	SiembraLabor labor = new SiembraLabor(store);
				labor.setLayer(new LaborLayer());
				Optional<Margen> cosechaConfigured= MargenConfigDialogController.config(labor);
				if(!cosechaConfigured.isPresent()){//
					System.out.println(Messages.getString("JFXMain.317")); //$NON-NLS-1$
					continue;
				}							

				OpenMargenMapTask umTask = new OpenMargenMapTask(labor);
				umTask.installProgressBar(progressBox);

				//	testLayer();
				umTask.setOnSucceeded(handler -> {
					Margen ret = (Margen)handler.getSource().getValue();
					margenes.add(ret);
					insertBeforeCompass(getWwd(), ret.getLayer());
					this.getLayerPanel().update(this.getWwd());
					umTask.uninstallProgressBar();
					viewGoTo(ret);

					System.out.println(Messages.getString("JFXMain.318")); //$NON-NLS-1$
					playSound();
				});//fin del OnSucceeded
				//umTask.start();
				JFXMain.executorPool.execute(umTask);
			}//fin del for stores

		}//if stores != null

	}

	private void doProcessMargin() {		
		System.out.println(Messages.getString("JFXMain.319")); //$NON-NLS-1$

		Margen margen = new Margen();
		margen.setLayer(new LaborLayer());

		//todo pasar el filtrado por visibles aca y pasar nuevas listas solo con las visibles
		List<PulverizacionLabor> pulvEnabled = getPulverizacionesSeleccionadas();//pulverizaciones.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
		List<FertilizacionLabor> fertEnabled = getFertilizacionesSeleccionadas();//fertilizaciones.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
		List<SiembraLabor> siemEnabled = getSiembrasSeleccionadas();//t());
		List<CosechaLabor> cosechasEnabled = getCosechasSeleccionadas();//cosechas.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());


		margen.setFertilizaciones(fertEnabled);
		margen.setPulverizaciones(pulvEnabled);
		margen.setSiembras(siemEnabled);
		margen.setCosechas(cosechasEnabled);

		StringBuilder sb = new StringBuilder();
		sb.append(Messages.getString("JFXMain.320")); //$NON-NLS-1$
		cosechasEnabled.forEach((c)->sb.append(c.getNombre()+Messages.getString("JFXMain.321"))); //$NON-NLS-1$
		margen.setNombre(sb.toString());

		Optional<Margen> margenConfigured= MargenConfigDialogController.config(margen);
		if(!margenConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.322")); //$NON-NLS-1$
			return;
		}							


		ProcessMarginMapTask uMmTask = new ProcessMarginMapTask(margen);
		//	ProgressBar progressBarTask = new ProgressBar();
		uMmTask.installProgressBar(progressBox);
		//progressBox.getChildren().add(progressBarTask);
		//		progressBarTask.setProgress(0);
		//	progressBarTask.progressProperty().bind(uMmTask.progressProperty());
		//			Thread currentTaskThread = new Thread(uMmTask);
		//			currentTaskThread.setDaemon(true);
		//			currentTaskThread.start();

		uMmTask.setOnSucceeded(handler -> {
			Margen ret = (Margen)handler.getSource().getValue();
			uMmTask.uninstallProgressBar();

			this.margenes.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());

			playSound();

			viewGoTo(ret);

			System.out.println(Messages.getString("JFXMain.323")); //$NON-NLS-1$
		});
		executorPool.execute(uMmTask);
	}

	private void doEditMargin(Margen margen) {		
		System.out.println(Messages.getString("JFXMain.324")); //$NON-NLS-1$
		Optional<Margen> margenConfigured= MargenConfigDialogController.config(margen);
		if(!margenConfigured.isPresent()){//
			System.out.println(Messages.getString("JFXMain.325")); //$NON-NLS-1$
			return;
		}							
		OpenMargenMapTask uMmTask = new OpenMargenMapTask(margen);
		uMmTask.installProgressBar(progressBox);
		uMmTask.setOnSucceeded(handler -> {
			this.getLayerPanel().update(this.getWwd());
			uMmTask.uninstallProgressBar();
			this.wwjPanel.repaint();
			System.out.println(Messages.getString("JFXMain.326")); //$NON-NLS-1$
			playSound();
		});
		executorPool.execute(uMmTask);
	}

	private void doProcesarBalanceNutrientes() {		
		System.out.println(Messages.getString("JFXMain.327")); //$NON-NLS-1$
		//todo pasar el filtrado por visibles aca y pasar nuevas listas solo con las visibles
		List<Suelo> suelosEnabled = getSuelosSeleccionados();
		List<FertilizacionLabor> fertEnabled = getFertilizacionesSeleccionadas();
		//List<SiembraLabor> siemEnabled = siembras.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());
		List<CosechaLabor> cosechasEnabled = cosechas.stream().filter((l)->l.getLayer().isEnabled()).collect(Collectors.toList());

		//		Suelo suelo = new Suelo();
		//		suelo.setLayer(new LaborLayer());
		//		StringBuilder sb = new StringBuilder();
		//		sb.append("Suelo ");
		//		cosechasEnabled.forEach((c)->sb.append(c.getNombreProperty().get()+" "));
		//		suelo.getNombreProperty().setValue(sb.toString());

		//		Optional<Margen> margenConfigured= MargenConfigDialogController.config(margen);
		//		if(!margenConfigured.isPresent()){//
		//			System.out.println("el dialogo termino con cancel asi que no continuo con el calculo del balance de fosforo");
		//			return;
		//		}							


		ProcessBalanceDeNutrientes balanceNutrientesTask = new ProcessBalanceDeNutrientes(suelosEnabled,
				cosechasEnabled, fertEnabled);

		balanceNutrientesTask.installProgressBar(progressBox);

		balanceNutrientesTask.setOnSucceeded(handler -> {
			Suelo ret = (Suelo)handler.getSource().getValue();
			balanceNutrientesTask.uninstallProgressBar();

			this.suelos.add(ret);
			insertBeforeCompass(getWwd(), ret.getLayer());
			this.getLayerPanel().update(this.getWwd());

			playSound();
			viewGoTo(ret);
			System.out.println(Messages.getString("JFXMain.328")); //$NON-NLS-1$
		});
		executorPool.execute(balanceNutrientesTask);
	}



	private void doExportLabor(Labor<?> laborToExport) {
//		if(labor==null){//esto servia cuando solo se elegia exportar pero no la cosecha
//			Optional<CosechaLabor> cosechaSelected = HarvestSelectDialogController.select(this.cosechas);
//			if(cosechaSelected.isPresent()){
//				labor= cosechaSelected.get();
//			} else {
//				return;
//			}
//		}

		//final Labor<?> laborToExport = labor;

		String nombre = laborToExport.getNombre();
		File shapeFile =  FileHelper.getNewShapeFile(nombre);

		ExportLaborMapTask ehTask = new ExportLaborMapTask(laborToExport,shapeFile);
		executorPool.execute(ehTask);
	}


	//TODO permitir al ususario definir el formato. para siembra fertilizada necesita 3 columnas
	//en la linea, al costado de la linea, siembra
	private void doExportPrescripcionFertilizacion(FertilizacionLabor laborToExport) {
		String nombre = laborToExport.getNombre();
		File shapeFile =  FileHelper.getNewShapeFile(nombre);
		executorPool.execute(()->ExportarPrescripcionFertilizacionTask.run(laborToExport, shapeFile));		
	}

	private void doExportPrescripcionSiembra(SiembraLabor laborToExport) {
		String nombre = laborToExport.getNombre();
		File shapeFile = FileHelper.getNewShapeFile(nombre);

		executorPool.execute(()->ExportarPrescripcionSiembraTask.run(laborToExport, shapeFile));
	}
	

	private void doExportHarvestDePuntos(CosechaLabor laborToExport) {
//		if(laborToExport==null){
//			Optional<CosechaLabor> cosechaSelected = HarvestSelectDialogController.select(this.cosechas);
//			if(cosechaSelected.isPresent()){
//				laborToExport= cosechaSelected.get();
//			} else {
//				return;
//			}
//		}
		
		String nombre = laborToExport.getNombre();
		File shapeFile = FileHelper.getNewShapeFile(nombre);

		executorPool.execute(()->ExportarCosechaDePuntosTask.run(laborToExport, shapeFile));
		}

	
/**
 * metodo que toma una labor y muestra una tabla con los campos de la labor
 * @param labor
 */
	private void doShowDataTable(Labor<?> labor) {		   
		SmartTableView.showLaborTable(labor);
	}

	
	private void doSnapshot(){
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);

		WritableImage image = wwNode.snapshot(params, null);
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Messages.getString("JFXMain.395")); //$NON-NLS-1$


		File lastFile = null;
		String lastFileName =  config.getPropertyOrDefault(Configuracion.LAST_FILE,Messages.getString("JFXMain.396")); //$NON-NLS-1$
		if(lastFileName != Messages.getString("JFXMain.397")){ //$NON-NLS-1$
			lastFile = new File(lastFileName);
		}
		if(lastFile ==null || ! lastFile.exists()) {
			lastFile=File.listRoots()[0];
		} 
		//if(lastFile != null && lastFile.exists()){
		fileChooser.setInitialDirectory(lastFile.getParentFile());
		fileChooser.setInitialFileName(lastFile.getName());


		//	if(file!=null)		fileChooser.setInitialDirectory(file.getParentFile());
		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				Messages.getString("JFXMain.398"), Messages.getString("JFXMain.399")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.getExtensionFilters().add(extFilter);
		// Show save file dialog
		File snapsthotFile = fileChooser.showSaveDialog(JFXMain.stage);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), Messages.getString("JFXMain.400"), snapsthotFile); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private void playSound() {
		executorPool.execute(()->{
			try	{
				URL url = JFXMain.class.getClassLoader().getResource(SOUND_FILENAME); //ok en el jar!!
				AudioInputStream ais =  AudioSystem.getAudioInputStream(url); 

				//				InputStream bufferedIn = new BufferedInputStream(getClass().getResourceAsStream(SOUND_FILENAME));
				//				AudioInputStream inputStream = AudioSystem
				//						.getAudioInputStream(bufferedIn);
				Clip clip = AudioSystem.getClip();
				clip.open(ais);
				clip.start();
			}catch (Exception e){
				e.printStackTrace();
			}});
	}

	public void addDragAndDropSupport(){
		scene.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				//System.out.println("dragging");
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					event.acceptTransferModes(TransferMode.ANY);
				} else {
					event.consume();
				}
			}
		});

		// Dropping over surface
		scene.setOnDragDropped(new EventHandler<DragEvent>() {
			//Configuracion config = Configuracion.getInstance();
			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasFiles()) {
					success = true;

					//	String filePath = null;
					FileNameExtensionFilter filter = new FileNameExtensionFilter(Messages.getString("JFXMain.421"),Messages.getString("JFXMain.422")); //$NON-NLS-1$ //$NON-NLS-2$
					List<File> shpFiles = db.getFiles();
					shpFiles.removeIf(f->{
						return !filter.accept(f);
					});
					// update Configuracion.lasfFile

					if(shpFiles.size()>0){
						File lastFile = shpFiles.get(shpFiles.size()-1);

						config.setProperty(Configuracion.LAST_FILE, lastFile.getAbsolutePath());
						config.save();
						doOpenCosecha(shpFiles);//ok!
					}



					FileNameExtensionFilter tifFilter = new FileNameExtensionFilter(Messages.getString("JFXMain.423"),Messages.getString("JFXMain.424")); //$NON-NLS-1$ //$NON-NLS-2$
					List<File> tifFiles = db.getFiles();
					tifFiles.removeIf(f->!tifFilter.accept(f));
					// update Configuracion.lasfFile
					//	Configuracion config = Configuracion.getInstance();
					if(tifFiles.size()>0){
						File lastFile = tifFiles.get(tifFiles.size()-1);

						config.setProperty(Configuracion.LAST_FILE, lastFile.getAbsolutePath());
						config.save();
						tifFiles.stream().forEach((f)->showNdviTiffFile(f,null,null));
					}

					//ok!

				}
				event.setDropCompleted(success);
				event.consume();
			}
		});

	}

	public static void main(String[] args) {
		Application.launch(JFXMain.class, args);
	}
}
