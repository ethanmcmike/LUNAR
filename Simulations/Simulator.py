from matplotlib import pyplot as plt
import math as math
import numpy as np

## Rocket Mass/Dimensions Estimate
## Senior Design - Rocket Competition
## 16 September 2019

## DESCRIPTION
## Runs a physics simulation with time steps
## Models dynamic engine thrust and air drag
## Tracks position, velocity, and acceleration
## Evaluates launches based on test conditions

## LAUNCH FAILURE CONDITIONS
## Velocity is less than 15.8 m/s at rail exit
## Velocity exceeds Mach 1.0 at any point in flight
## Height does not reach 3500ft
## Height exceeds 5500ft

## INSTRUCTIONS
## Enter engine specs
## Modify engine timing to aproximate engine thrust graph
## Set test mass ranges
## Adjust simulator timing step for slower/faster simulations
## Read console output for test results

## USES SI UNITS

#Engine specs
Impulse = 2772      #N-s
BurnTime = 4.3      #s
Fmax = 1590         #N
Favg = Impulse / BurnTime

#Engine timing
t0 = 0.0            #Ignition
t2 = 0.5            #Max thrust
t3 = 0.8            #Start of average thrust
t4 = BurnTime       #End of burn

#Testing ranges
minMass = 0         #Starting mass to evaluate
maxMass = 20        #Ending mass to evaluate
massStep = 0.5      #Step between masses
graphs = [16]       #Masses to stop sim and display graphs

#Simulator timing
start   = t0        #Ignore...resets anyways in simulation
step    = 0.001     #Time between physics updates
t = start           #Simulator time variable

#Criteria
ytf = 1067      #Target height floor (3500 ft)
ytc = 1676      #Target height ceiling (5500 ft)
mvre = 15.8     #Min velocity at rail exit (52 m/s)
Mmax = 1.0      #Max mach speed

#Physical properties
m = 27          #Mass
D = 0.09        #Diameter
L = 2
A = math.pi * D * D / 4
Lrail = 3.658   #Rail length (12ft)

g = 9.80665     #Acceleration of gravity
k = 1.4         #Cv/Cp for air
R = 287
Cd = 0.75       #Coefficient of drag

#Air temperature as a function of altitude
#Valid from 0 to 11km
#Kelvin
def temp(h):

    #A = 0.0065 between 0 and 10km
    if(0 < h and h < 11000):
        return 288.16 - 0.0065 * h

    #Standard temp
    else:
        return 288.16

#Air density as a function of altitude
#Valid from 0 to 11km
#km/m^3
def density(h):
    return 1.225 * (temp(h)/temp(0))**(-((g/(-0.0065*R))+1))

#Speed of sound as a function of altitude
def va(h):
    return math.sqrt(k * R * temp(h))

#Physics
y = 0.0
v = 0.0
a = 0.0

###Stats
##my = 0          #Min height
##My = 0          #Max height
##mv = 0          #Min velocity
##Mv = 0          #Max velocity
##MM = 0          #Max mach number


#Force due to engine thrust
#Newtons
def thrust():

    #Spiking to max thrust
    if(0 <= t and t < t2):
        return Fmax/t2 * t

    #Decreasing to average thrust
    elif(t2 <= t and t < t3):
        return (Favg-Fmax)/(t3-t2)*(t-t2) + Fmax

    #Cruise at average thrust
    elif(t3 <= t and t < t4):
        return Favg

    #End of burn
    else:
        return 0

#Force due to air drag
#Newtons
def drag():
    return -np.sign(v) * Cd * density(y) * A * v * v / 2

#Force due to gravity
#Newtons
def gravity():
    return -m*g

#Force due to friction on launch rail
def friction():
    return 0

#Total force as a function of time and position
def force():

    #Prior to lift off
    #Upward reaction force of launch pad makes it static
    if(t < t2 and thrust() < abs(gravity())):
        return 0

    #Lift-off along rail
    #Ignore air drag until off rail
    if(y < Lrail):
        return thrust() + gravity() + friction()

    #Climb
    if(t < t4):
        return thrust() + gravity() + drag()

    #Descent
    if(t > t4):
        return gravity()

    return 0

#Updates physics
def update(dt):
    global a, v, y
    
    v = v + a*dt
    y = y + v*dt

#Simulate
print("Simulation started...")

for mass in np.arange(minMass, maxMass, massStep):

    m = mass

    #Log position for graph
    graphT = []
    graphY = []
    graphV = []
    graphA = []
    graphFT = []

    #Reset physics
    t = start

    y = 0.0
    v = 0.0
    a = 0.0

    my = 0          #Min height
    My = 0          #Max height
    mv = 0          #Min velocity
    Mv = 0          #Max velocity
    MM = 0          #Max mach number
    vre = 0         #Rail exit velocity

    #Step through time
    while(y >= 0 and v >= 0):
        a = force() / m
        update(step)

        if m in graphs:
            graphT.append(t)
            graphY.append(y)
            graphV.append(v)
            graphA.append(a)
            graphFT.append(thrust())

        #Record height/velocity limits
        if(y < my):
            my = y
        if(y > My):
            My = y
        if(v < mv):
            mv = v
        if(v > Mv):
            Mv = v

        #Record rail exit velocity
        if(y < Lrail and t < t3):
            vre = v

        #Record max mach number
        mach = v / va(y)
        if(mach > MM):
            MM = mach

        t += step

    result = "Passed"
    
    if(MM > Mmax):
        result = "Exceeded Mach 1.0"
    elif(vre < mvre):
        result = "Did not meet min launch velocity"
    elif(My < ytf):
        result = "Did not reach min altitude"
    elif(My > ytc):
        result = "Exceeded max altitude"

    print("Mass = %5.2fkg %s" % (m, result))

    if m in graphs:
        #Show thrust graph
        plt.figure("Thrust")
        plt.suptitle("Mass = %5.2fkg %s" % (m, result))
        plt.plot(graphT, graphFT, 'orange')
        plt.ylabel("Thrust [n]")
        plt.xlabel("Time [s]")
        plt.axvline(x=t2, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t2, 0, "t2", size=9)
        plt.axvline(x=t3, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t3, 0, "t3", size=9)
        plt.axvline(x=t4, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t4, 0, "t4", size=9)

        #Show acceleration graph
        plt.figure("Acceleration")
        plt.suptitle("Mass = %5.2fkg %s" % (m, result))
        plt.plot(graphT, graphA, 'g')
        plt.ylabel("Acceleration [m/s^2]")
        plt.xlabel("Time [s]")
        plt.axvline(x=t2, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t2, 0, "t2", size=9)
        plt.axvline(x=t3, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t3, 0, "t3", size=9)
        plt.axvline(x=t4, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t4, 0, "t4", size=9)

        #Show velocity graph
        plt.figure("Velocity")
        plt.suptitle("Mass = %5.2fkg %s" % (m, result))
        plt.plot(graphT, graphV, 'b')
        plt.ylabel("Velocity [m/s]")
        plt.xlabel("Time [s]")
        plt.axvline(x=t2, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t2, 0, "t2", size=9)
        plt.axvline(x=t3, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t3, 0, "t3", size=9)
        plt.axvline(x=t4, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t4, 0, "t4", size=9)
        plt.axhline(y=mvre, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(0, mvre, "Min Rail Exit", size=9)
        
        #Show height graph
        plt.figure("Height")
        plt.suptitle("Mass = %5.2fkg %s" % (m, result))
        plt.plot(graphT, graphY, 'r')
        plt.ylabel("Height [m]")
        plt.xlabel("Time [s]")
        plt.axvline(x=t2, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t2, 0, "Max Thrust", size=9)
        plt.axvline(x=t3, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t3, 0, "Avg Thrust", size=9)
        plt.axvline(x=t4, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(t4, 0, "End Thrust", size=9)
        plt.axhline(y=ytc, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(0, ytc, "Target Ceiling", size=9)
        plt.axhline(y=ytf, color="black", linestyle="--", label="t1", linewidth=1)
        plt.text(0, ytf, "Target Floor", size=9)

        plt.show()

print("Simulation completed!")
