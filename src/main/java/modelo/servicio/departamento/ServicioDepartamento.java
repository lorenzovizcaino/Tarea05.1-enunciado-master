package modelo.servicio.departamento;

import java.util.List;

import modelo.Departamento;
import modelo.dao.departamento.DepartamentoEXistDao;
import modelo.dao.departamento.IDepartamentoDao;
import modelo.exceptions.DuplicateInstanceException;
import modelo.exceptions.InstanceNotFoundException;
import org.exist.xmldb.EXistResource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XQueryService;
import util.ConnectionManager;
import util.MyDataSource;

public class ServicioDepartamento implements IServicioDepartamento {

	private IDepartamentoDao departamentoDao;

	public ServicioDepartamento() {
		departamentoDao = new DepartamentoEXistDao();
	}

	@Override
	public boolean create(Departamento dept) throws DuplicateInstanceException {
			int contador=comprobarExistencia(dept);
			if (contador>0){
				throw new DuplicateInstanceException("Ya existe un departamento con este id",dept.getDeptno(),Departamento.class.getName());


			}else {
				return departamentoDao.create(dept);
			}

	}

	private int comprobarExistencia(Departamento dept) {
		MyDataSource dataSource = ConnectionManager.getDataSource();
		int contador = 0;
		Collection col = null;
		try {
			col = DatabaseManager.getCollection(dataSource.getUrl() + dataSource.getColeccionDepartamentos());
			XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
			xqs.setProperty("indent", "yes");
			String query="for $x in doc('departamentos.xml')//DEPT_NO where $x/.= "+dept.getDeptno()+" return count($x)";
			CompiledExpression compiled = xqs.compile(query);
			ResourceSet result = xqs.execute(compiled);
			ResourceIterator i = result.getIterator();
			Resource res = null;
			while(i.hasMoreResources()) {
				try {
					res = i.nextResource();
					contador= Integer.parseInt((String) res.getContent());

				} finally {
					//dont forget to cleanup resources
					try { ((EXistResource)res).freeResources(); } catch(XMLDBException xe) {xe.printStackTrace();}
				}
			}
		} catch (XMLDBException e) {
			throw new RuntimeException(e);
		} finally {
			//dont forget to cleanup
			if(col != null) {
				try { col.close(); } catch(XMLDBException xe) {xe.printStackTrace();}
			}
		}
		return contador;
	}

	@Override
	public boolean delete(Departamento dept) {
		return departamentoDao.delete(dept);
	}

	@Override
	public boolean update(Departamento dept) {
		return departamentoDao.update(dept);
	}

	@Override
	public List<Departamento> findAll() {
		return departamentoDao.findAll();
	}

	

	@Override
	public Departamento read(long deptno) throws InstanceNotFoundException {
		return departamentoDao.read(deptno);
	}

}
