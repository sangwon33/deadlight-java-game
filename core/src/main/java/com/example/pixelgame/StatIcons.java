package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Small silhouettes share the existing action icons' outline and muted metal palette. */
final class StatIcons {
    private static final Color[] COLORS={new Color(.91f,.49f,.53f,1),UiTheme.TEXT,
            new Color(.66f,.82f,.94f,1),new Color(.59f,.88f,.83f,1),
            new Color(.93f,.79f,.48f,1),new Color(.98f,.61f,.4f,1)};
    private static final float[][] HEART={{-6,2},{-6,4},{-4,6},{-1,6},{0,4},{1,6},{4,6},{6,4},{6,1},{0,-6},{-6,1}};
    private static final float[][] BOOT={{-3,6},{1,6},{1,0},{4,-2},{7,-2},{7,-5},{-4,-5},{-5,-3},{-3,0}};
    private StatIcons(){}
    static void draw(SpriteBatch b,UiTheme ui,int index,float x,float y){
        shape(b,ui,index,x-.7f,y,UiTheme.SURFACE);shape(b,ui,index,x+.7f,y,UiTheme.SURFACE);
        shape(b,ui,index,x,y-.7f,UiTheme.SURFACE);shape(b,ui,index,x,y+.7f,UiTheme.SURFACE);
        shape(b,ui,index,x,y,COLORS[index]);
    }
    private static void shape(SpriteBatch b,UiTheme ui,int index,float x,float y,Color c){
        switch(index){
            case 0 -> {
                polygon(b,ui,x,y,HEART,c);
            }
            case 1 -> sword(b,ui,x,y,c);
            case 2 -> {
                polygon(b,ui,x,y,BOOT,c);
                ui.line(b,x-3,y-3,x+5,y-3,.65f,UiTheme.SURFACE,.8f);
            }
            case 3 -> {
                sword(b,ui,x+1,y,c);
                ui.line(b,x-8,y+4,x-4,y+4,1.1f,c,1);
                ui.line(b,x-9,y,x-5,y,1.1f,c,1);
                ui.line(b,x-7,y-4,x-4,y-4,1.1f,c,1);
            }
            case 4 -> {
                circle(b,ui,x-2,y,4.2f,c);
                ui.line(b,x-8,y,x-5,y,1,c,1);ui.line(b,x-2,y-6,x-2,y-3,1,c,1);
                ui.line(b,x-2,y+3,x-2,y+6,1,c,1);ui.rect(b,x-2.6f,y-.6f,1.2f,1.2f,c,1);
                ui.line(b,x+3,y-3,x+7,y+3,1,c,1);
                circle(b,ui,x+3,y+2.7f,.8f,c);circle(b,ui,x+7,y-2.7f,.8f,c);
            }
            case 5 -> {
                for(int i=0;i<8;i++){
                    double a=Math.toRadians(i*45),l=a-.24,r=a+.24;
                    ui.triangle(b,x+(float)Math.cos(a)*8,y+(float)Math.sin(a)*8,
                            x+(float)Math.cos(l)*4,y+(float)Math.sin(l)*4,
                            x+(float)Math.cos(r)*4,y+(float)Math.sin(r)*4,c,1);
                }
                sword(b,ui,x,y,c);
            }
            default -> { }
        }
    }
    private static void sword(SpriteBatch b,UiTheme ui,float x,float y,Color c){
        ui.triangle(b,x-1,y-1,x+6,y+7,x+1,y-2,c,1);
        ui.line(b,x-3,y,x+1,y-4,1.3f,c,1);
        ui.line(b,x-5,y-6,x-1,y-2,1.7f,c,1);
    }
    private static void polygon(SpriteBatch b,UiTheme ui,float x,float y,float[][] p,Color c){
        for(int i=0;i<p.length;i++){float[] a=p[i],n=p[(i+1)%p.length];ui.triangle(b,x,y,x+a[0],y+a[1],x+n[0],y+n[1],c,1);}
    }
    private static void circle(SpriteBatch b,UiTheme ui,float x,float y,float r,Color c){
        for(int i=0;i<16;i++){
            double a=Math.PI*2*i/16,d=Math.PI*2*(i+1)/16;
            ui.line(b,x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r,
                    x+(float)Math.cos(d)*r,y+(float)Math.sin(d)*r,.8f,c,1);
        }
    }
}
