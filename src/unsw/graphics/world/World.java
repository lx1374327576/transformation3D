package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D implements MouseListener,KeyListener{

    private Terrain terrain;

    private float pi=(float) 3.1415926535;
    private int times=1;

    private boolean first_or_third=true;
    private boolean if_look_ground=false;

    private float dx=(float)0.05;
    private float dy=(float)0.05;
    private float dz=(float)0.05;
    private int[][] direction={{1,0},{0,-1},{-1,0},{0,1}};
    private int now_direction=2;

    private float rotateX = 0;
    private float rotateY = 90;
    private float rotateZ = 0;
    private float r_rotateX = 0;
    private float r_rotateY = 0;
    private Point2D myMousePoint = null;
    private static final float ROTATION_SCALE = 1;

    private Texture texture;
    private Texture texture1;

    private TriangleMesh tree; //tree model
    private TriangleMesh model;//person

    private List<TriangleMesh> meshes =  new ArrayList<>();

    public World(Terrain terrain)  throws IOException {
    	super("Assignment 2", 800, 800);
        this.terrain = terrain;
        tree = new TriangleMesh("res/models/tree.ply", true, true); //load the tree model
        model = new TriangleMesh("res/models/bunny.ply", true, true);

    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException ,IOException  {
        Terrain terrain = LevelIO.load(new File("res/worlds/test1.json"));
        World world = new World(terrain);
        world.start();
    }

	@Override
	public void display(GL3 gl) {
		super.display(gl);

		Shader.setInt(gl, "tex", 0);

        gl.glActiveTexture(GL.GL_TEXTURE0);


        Shader.setPenColor(gl, Color.WHITE);
		// Set the lighting properties
        Shader.setPoint3D(gl, "lightPos", new Point3D(terrain.sunlight.getX(), terrain.sunlight.getY(), terrain.sunlight.getZ()));
        Shader.setColor(gl, "lightIntensity", Color.GREEN);
        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setFloat(gl, "phongExp", 16f);

        rotate_dy();
        float qx,qy,qz;
        if (first_or_third){
        	qx=0;
        	qy=(float) -2;
        	qz=0;
        }else{
        	qx=(float) 5;
        	qy=(float) -5;
        	qz=0;
        }
		CoordFrame3D frame = CoordFrame3D.identity()
				.rotateX((float)rotateX)
                .rotateY((float)rotateY)
                .translate((float)(qx-dx),(float) (qy-dy),(float)(qz-dz))
                .scale(times, times, times);
		//System.out.println(dx+" "+dz+" "+now_direction);
		//System.out.println("lx"+rotateX);

		gl.glBindTexture(GL.GL_TEXTURE_2D, texture1.getId());
		for (TriangleMesh mesh : meshes)
            mesh.draw(gl, frame);

		gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
		//draw the trees
		for (int i=0;i<terrain.trees.size();i++) {
			float x = terrain.trees.get(i).getPosition().getX();
			float z = terrain.trees.get(i).getPosition().getZ();
			int x1 = (int)x;
			int x2 = (int)x+1;
			int z1 = (int)z;
			int z2 = (int)z+1;
			float y = (float)(terrain.getGridAltitude(x2, z1) + terrain.getGridAltitude(x1, z2))/2;

			CoordFrame3D treeFrame =
	                frame.translate((float)x, (float)y+0.5f, (float)z).scale(0.1f, 0.1f, 0.1f);

			tree.draw(gl, treeFrame);
		}

		CoordFrame3D modelFrame = frame
//				.rotateY(r_rotateY)
				.translate((float)(0+dx),(float)(0.3+dy),(float)(0+dz))
				.scale(5, 5, 5);
		if (!first_or_third)model.draw(gl,modelFrame);

		//System.out.println("tsy"+rotateX);



	}

	private void rotate_dy() {
		// TODO Auto-generated method stub
		float y1=terrain.altitudes[(int)dx][(int)dz];
		float y2=terrain.altitudes[(int)dx+1][(int)dz+1];
		float ddz=dz-(int)dz;
		float ddx=dx-(int)dx;
		float y3;
		if (ddz>ddx){
			y3=terrain.altitudes[(int)dx][(int)dz+1];
			dy=(y2-y3)*ddx+(y3-y1)*ddz+y1;
			switch (now_direction){
				case 2:
					rotateX=(float) Math.atan(y3-y2)/pi*180;
					break;
				case 1:
					rotateX=(float) Math.atan(y3-y1)/pi*180;
					break;
				case 0:
					rotateX=(float) Math.atan(y2-y3)/pi*180;
					break;
				case 3:
					rotateX=(float) Math.atan(y1-y3)/pi*180;
					break;
			}
		}else{
			y3=terrain.altitudes[(int)dx+1][(int)dz];
			dy=(y2-y3)*ddz+(y3-y1)*ddx+y1;
			switch (now_direction){
				case 0:
					rotateX=(float) Math.atan(y1-y3)/pi*180;
					break;
				case 3:
					rotateX=(float) Math.atan(y2-y3)/pi*180;
					break;
				case 2:
					rotateX=(float) Math.atan(y3-y1)/pi*180;
					break;
				case 1:
					rotateX=(float) Math.atan(y3-y2)/pi*180;
					break;
			}
		rotateX=-rotateX;
		}
		if (!first_or_third)rotateX=45;
		if (if_look_ground)rotateX+=45;
		//System.out.println(y1+" "+y2+" "+y3+" "+ddx+" "+ddz);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);

	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		tree.init(gl);
		model.init(gl);
		texture = new Texture(gl, "res/textures/BrightPurpleMarble.png", "png", false);
		texture1 = new Texture(gl, "res/textures/grass.bmp", "bmp", false);
//		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
//                "shaders/fragment_tex_phong.glsl");

		Shader shader = new Shader(gl, "shaders/vertex_tex_3d.glsl",
                "shaders/fragment_tex_3d.glsl");

		//getWindow().addMouseListener(this);
		getWindow().addKeyListener(this);

		shader.use(gl);

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
		for (int i=0;i<terrain.width;i++) {
			for (int j=0;j<terrain.depth;j++){
				mapShape.add(new Point3D(i,terrain.altitudes[i][j],j));
			}
		}
		List<Integer> mapShapeIndices=new ArrayList<Integer>();
		for (int i=0;i<terrain.width-1;i++)
			for (int j=0;j<terrain.depth-1;j++){
				mapShapeIndices.add(i*terrain.depth+j);

				mapShapeIndices.add((i+1)*terrain.depth+j+1);
				mapShapeIndices.add((i+1)*terrain.depth+j);

				mapShapeIndices.add((i+1)*terrain.depth+j+1);
				mapShapeIndices.add(i*terrain.depth+j);
				mapShapeIndices.add(i*terrain.depth+j+1);
			}



		TriangleMesh map = new TriangleMesh(mapShape, mapShapeIndices, true);
		map.init(gl);
		meshes.add(map);
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

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		switch (e.getKeyCode()) {

			case KeyEvent.VK_W:
				dx-=direction[now_direction][0]*0.1;
				dz+=direction[now_direction][1]*0.1;
				if (!(dx<=terrain.width*times-1.1 && dx>=0 && dz<=terrain.depth*times-1.1 && dz>=0)){
					dx+=direction[now_direction][0]*0.1;
					dz-=direction[now_direction][1]*0.1;
				}
				break;
			case KeyEvent.VK_S:
				dx+=direction[now_direction][0]*0.1;
				dz-=direction[now_direction][1]*0.1;
				if (!(dx<=terrain.width*times-1.1 && dx>=0 && dz<=terrain.depth*times-1.1 && dz>=0)){
					dx-=direction[now_direction][0]*0.1;
					dz+=direction[now_direction][1]*0.1;
				}
				break;
			case KeyEvent.VK_A:
				now_direction=(now_direction+3)%4;
				if (first_or_third){
					rotateY-=90;
					if (rotateY<0) rotateY+=360;
				}else{
					r_rotateY-=90;
					if (r_rotateY<0) r_rotateY+=360;
				}
				break;
			case KeyEvent.VK_D:
				now_direction=(now_direction+1)%4;
				if (first_or_third){
					rotateY+=90;
					if (rotateY>=360) rotateY-=360;
				}else{
					r_rotateY+=90;
					if (r_rotateY>=360) r_rotateY-=360;
				}
				break;
			case KeyEvent.VK_Y:
				first_or_third=!first_or_third;
				break;
			case KeyEvent.VK_T:
				if_look_ground=!if_look_ground;
				break;
//			case KeyEvent.VK_1:
//				if ( dy >-100 ) dy -= 0.5;
//				break;
//			case KeyEvent.VK_2:
//				if ( dy < 100 ) dy += 0.5;
//				break;
			default:
				break;

        }
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
  }
