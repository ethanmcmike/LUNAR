from scipy.optimize import curve_fit
import math
import numpy as np
from matplotlib import pyplot as plt

m = 1
g = 9.81
A = 0.6*0.1
density = 1.225

startPitch = 0
stopPitch  = 90
stepPitch  = 0.5

NACA_X = [0, 1, 2, 3, 4, 5]
NACA_Y = [.123, .124, .034, .013, .104, 0.245]

def Cd(Cl, a, b, c):
    return a*Cl*Cl + b*Cl + c

#Required Cl/Cd to maintain equilibrium at given pitch in degrees
def Cld(p):
    p = p*math.pi/180
    return 1/math.tan(p)

#Speed required to generate enough lift at given pitch to fall in equilibrium
def Vt(m, g, density, A, Cl, Cd, pitch):
    pitch = pitch*math.pi/180
    return math.sqrt((2*m*g) / (density*A*(
            Cl*math.cos(pitch)+
            Cd*math.sin(pitch))))

#Find coefficient of lift given pitch and airfoil drag function coefficients
def Cl(p, a, b, c):
    p = p * math.pi / 180
    l = ((math.sqrt(
            -(4*a*c-b*b+1)*math.cos(p)*math.cos(p)
            -2*b*math.sin(p)*math.cos(p)
            +1-b*math.cos(p))
            +math.sin(p))
            /(2*a*math.cos(p)))
    d = l*math.tan(p)
    return l, d
            

#Fit curve for given NACA airfoil data
coefs, pcov = curve_fit(Cd, NACA_X, NACA_Y)

#Generate Vy vs pitch data
for angle in np.arange(startPitch, stopPitch, stepPitch):

    
    print(Cl(angle, *coefs))
    pass
    
#Display Vy vs pitch

