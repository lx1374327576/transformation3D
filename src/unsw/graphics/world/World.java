package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLProfile;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D implements MouseListener{

    private Terrain terrain;

    private float rotateX = 0;
    private float rotateY = 0;
    private Point2D myMousePoint = null;
    private static final float ROTATION_SCALE = 1;

    private List<TriangleMesh> meshes =  new ArrayList<>();

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 800);
        this.terrain = terrain;

    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File("res/worlds/test1.json"));
        World world = new World(terrain);
        world.start();
    }

	@Override
	public void display(GL3 gl) {
		super.display(gl);
		CoordFrame3D frame = CoordFrame3D.identity()
                .translate(-5,-5,-15)
                .rotateX(rotateX)
                .rotateY(rotateY);

		Shader.setPenColor(gl, Color.GREEN);

		for (TriangleMesh mesh : meshes)
            mesh.draw(gl, frame);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);

	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		getWindow().addMouseListener(this);

		shaderset(gl);

		makeExtrusion(gl);

	}

	@Override
    public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, 1, 1, 100));
    }

	@Override
	public void mouseDragged(MouseEvent e) {
		Point2D p = new Point2D(e.getX(), e.getY());

        if (myMousePoint != null) {
            float dx = p.getX() - myMousePoint.getX();
            float dy = p.getY() - myMousePoint.getY();

            // Note: dragging in the x dir rotates about y
            //       dragging in the y dir rotates about x
            rotateY += dx * ROTATION_SCALE;
            rotateX += dy * ROTATION_SCALE;

        }
        myMousePoint = p;
	}

	private void makeExtrusion(GL3 gl){
		List<Point3D> mapShape=new ArrayList<Point3D>();
		for (int i=0;i<terrain.width;i++)
			for (int j=0;j<terrain.depth;j++){
				mapShape.add(new Point3D(i,j,terrain.altitudes[i][j]));
			}
		List<Integer> mapShapeIndices=new ArrayList<Integer>();
		for (int i=0;i<terrain.width-1;i++)
			for (int j=0;j<terrain.depth-1;j++){
				mapShapeIndices.add(i*terrain.depth+j);
				mapShapeIndices.add((i+1)*terrain.depth+j);
				mapShapeIndices.add((i+1)*terrain.depth+j+1);
				mapShapeIndices.add(i*terrain.depth+j);
				mapShapeIndices.add((i+1)*terrain.depth+j+1);
				mapShapeIndices.add(i*terrain.depth+j+1);
			}



		TriangleMesh map = new TriangleMesh(mapShape, mapShapeIndices, true);
		map.init(gl);
		meshes.add(map);
	}

	private void shaderset(GL3 gl){
        Shader shader = new Shader(gl, "shaders/vertex_phong.glsl",
                "shaders/fragment_phong.glsl");
        shader.use(gl);

        // Set the lighting properties
        Shader.setPoint3D(gl, "lightPos", new Point3D(0, 0, 5));
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setFloat(gl, "phongExp", 16f);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		 myMousePoint = new Point2D(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
  }
