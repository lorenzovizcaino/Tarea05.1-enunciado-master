/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.dao.departamento;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exist.xmldb.EXistResource;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.CompiledExpression;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XQueryService;

import modelo.Departamento;
import modelo.dao.AbstractGenericDao;
import modelo.exceptions.InstanceNotFoundException;
import util.ConnectionManager;
import util.MyDataSource;

/**
 *
 * @author mfernandez
 */
public class DepartamentoEXistDao extends AbstractGenericDao<Departamento> implements IDepartamentoDao {

	private static final String DEPT_ROW_TAG = "DEP_ROW";
	private static final String DEPT_NO_TAG = "DEPT_NO";
	private static final String DNOMBRE_TAG = "DNOMBRE";
	private static final String LOC_TAG = "LOC";
	private MyDataSource dataSource;

	public DepartamentoEXistDao() {
		this.dataSource = ConnectionManager.getDataSource();
		Class cl;
		try {
			cl = Class.forName(dataSource.getDriver());

			Database database = (Database) cl.newInstance();
			database.setProperty("create-database", "true");

			DatabaseManager.registerDatabase(database);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean create(Departamento entity) {
		boolean exito = false;

		String deptNodeString = toXMLString(entity);

		if (deptNodeString != "") {

			try (Collection col = DatabaseManager.getCollection(
					dataSource.getUrl() + dataSource.getColeccionDepartamentos(), dataSource.getUser(),
					dataSource.getPwd())) {

				XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
				xqs.setProperty("indent", "yes");

				CompiledExpression compiled = xqs
						.compile("update insert " + deptNodeString + "into doc(\"departamentos.xml\")//departamentos");
				xqs.execute(compiled);

				exito = true;

			} catch (XMLDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return exito;

	}

	@Override
	public Departamento read(long id) throws InstanceNotFoundException {
		Departamento departamento = null;

		try (Collection col = DatabaseManager.getCollection(
				dataSource.getUrl() + dataSource.getColeccionDepartamentos(), dataSource.getUser(),
				dataSource.getPwd())) {

			XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
			xqs.setProperty("indent", "yes");

			CompiledExpression compiled = xqs.compile("//DEP_ROW[DEPT_NO=" + id + "]");
			ResourceSet result = xqs.execute(compiled);

			if (result.getSize() == 0)
				throw new InstanceNotFoundException(id, Departamento.class.getName());

			ResourceIterator i = result.getIterator();
			Resource res = null;
			while (i.hasMoreResources()) {
				try {
					res = i.nextResource();

					System.out.println(res.getContent().toString());

					departamento = stringNodeToDepartamento(res.getContent().toString());

				} finally {
					// dont forget to cleanup resources
					try {
						((EXistResource) res).freeResources();
					} catch (XMLDBException xe) {
						departamento = null;
						xe.printStackTrace();
					}
				}
			}

		} catch (XMLDBException e) {
			departamento = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return departamento;
	}

	/**
	 * Toma un objeto Departamento y devuelve un nodo XML como el del documento
	 * departamentos.xml de src/main/resources <DEP_ROW> <DEPT_NO>10</DEPT_NO>
	 * <DNOMBRE>CONTABILIDAD</DNOMBRE> <LOC>SEVILLA</LOC> </DEP_ROW>en formato
	 * String
	 * 
	 * @param dept
	 * @return
	 */
	private String toXMLString(Departamento dept) {
		String output = "";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();

			DOMImplementation implementation = builder.getDOMImplementation();

			// Crea un document con un elmento raiz
			Document document = implementation.createDocument(null, DEPT_ROW_TAG, null);
			// Obtenemos el elemento raíz
			Element root = document.getDocumentElement();
			Element deptNo = createElement(document, DEPT_NO_TAG, String.valueOf(dept.getDeptno()));

			Element name = createElement(document, DNOMBRE_TAG, dept.getDname().trim());
			Element loc = createElement(document, LOC_TAG, dept.getLoc().trim());

			root.appendChild(deptNo);
			root.appendChild(name);
			root.appendChild(loc);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

			// https://docs.oracle.com/javase/8/docs/api/java/io/StringWriter.html
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			output = writer.getBuffer().toString().replaceAll("\n|\r", "");

			System.out.println(output);

		} catch (ParserConfigurationException e) {
			output = "";
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			output = "";
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return output;
	}

	private Element createElement(Document document, String tag, String content) {
		Element elemento = document.createElement(tag);
		elemento.setTextContent(content);
		return elemento;
	}

	private Departamento stringNodeToDepartamento(String nodeString) {
		Element node = null;
		Departamento departamento = null;
		try {
			node = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(nodeString.getBytes())).getDocumentElement();

			String nombre = getElementText(node, DNOMBRE_TAG);
			String location = getElementText(node, LOC_TAG);
			Integer id = Integer.parseInt(getElementText(node, DEPT_NO_TAG));

			departamento = new Departamento(nombre, location);
			departamento.setDeptno(id);

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return departamento;
	}

	private String getElementText(Element parent, String tag) {
		String texto = "";
		NodeList lista = parent.getElementsByTagName(tag);

		if (lista.getLength() > 0) {
			texto = lista.item(0).getTextContent();
		}

		return texto;
	}

	@Override
	public boolean update(Departamento entity) {
		boolean exito=false;
		int idDepartamento= entity.getDeptno();
		try (Collection col = DatabaseManager.getCollection(dataSource.getUrl() + dataSource.getColeccionDepartamentos(),
				dataSource.getUser(), dataSource.getPwd())) {

			XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");//and /LOC with '"+entity.getLoc()"'"
			xqs.setProperty("indent", "yes");

			CompiledExpression compiled = xqs.compile("update value //DEP_ROW[DEPT_NO='"+entity.getDeptno()+"']/DNOMBRE with'"+entity.getDname()+"'");
			ResourceSet result = xqs.execute(compiled);



			//if(result.getSize()==0) exito=true;
			exito=true;


		} catch (XMLDBException e) {

			e.printStackTrace();
		}

		return exito;
	}

	@Override
	public boolean delete(Departamento entity) {
		boolean exito=false;
		int idDepartamento= entity.getDeptno();
		try (Collection col = DatabaseManager.getCollection(dataSource.getUrl() + dataSource.getColeccionDepartamentos(),
				dataSource.getUser(), dataSource.getPwd())) {

			XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
			xqs.setProperty("indent", "yes");

			CompiledExpression compiled = xqs.compile("update delete //DEP_ROW[DEPT_NO="+idDepartamento+"]");
			ResourceSet result = xqs.execute(compiled);



			//if(result.getSize()==0) exito=true;
			exito=true;


		} catch (XMLDBException e) {

			e.printStackTrace();
		}

		return exito;
	}

	@Override
	public List<Departamento> findAll() {
		List <Departamento> departamentos=new ArrayList<>();
		Departamento departamento;

		try (Collection col = DatabaseManager.getCollection(dataSource.getUrl() + dataSource.getColeccionDepartamentos(),
				dataSource.getUser(), dataSource.getPwd())) {

			XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
			xqs.setProperty("indent", "yes");

			CompiledExpression compiled = xqs.compile("//DEP_ROW");
			ResourceSet result = xqs.execute(compiled);

			ResourceIterator i = result.getIterator();
			Resource res = null;
			while (i.hasMoreResources()) {
				try {
					res = i.nextResource();

					System.out.println(res.getContent().toString());

					departamento = stringNodeToDepartamento(res.getContent().toString());
					departamentos.add(departamento);

				} finally {
					// dont forget to cleanup resources
					try {
						((EXistResource) res).freeResources();
					} catch (XMLDBException xe) {
						departamento = null;
						xe.printStackTrace();
					}
				}
			}

		} catch (XMLDBException e) {
			departamento = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return departamentos;
	}

}
