from com.orbit.core import EntityScript

class Entity(EntityScript):

    def __init__(self):
        self.data = {}

    def setPosition(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z
   
    def setDirection(self, x, y, z):
        self.dirx = x
        self.diry = y
        self.dirz = z

    def onInteract(self):
        self.data['newMessage'] = "Hi!"
        return self.data

    def update(self):

        if (self.diry == -1): #going N
            if (self.y > 5):
                self.data['deltaY'] = '-1'
            else:
                self.data['deltaY'] = '0'
                self.data['deltaX'] = '1'
                self.data['halt'] = '500000000'
        elif (self.dirx == 1): #going E
            if (self.x < 200):
                self.data['deltaX'] = '1'
            else:
                self.data['deltaX'] = '0';
                self.data['deltaY'] = '1';
                self.data['halt'] = '500000000';
        elif (self.diry == 1): #going S
            if (self.y < 200):
                self.data['deltaY'] = '1'
            else:
                self.data['deltaY'] = '0'
                self.data['deltaX'] = '-1'
                self.data['halt'] = '500000000';
        elif (self.dirx == -1): #going W
            if (self.x > 5):
                self.data['deltaX'] = '-1'
            else:
                self.data['deltaX'] = '0'
                self.data['deltaY'] = '-1'
                self.data['halt'] = '500000000'

        return self.data
