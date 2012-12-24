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

    def setStage(self, stage, dummy):
        self.stage = stage
        
    def onInteract(self):
        if (self.stage == 0):
            self.data['newMessage'] = "Must find that book... where could it be... what was the title again? Oh, hello."
        elif (self.stage == 1):
            self.data['newMessage'] = "WE. GON'. DIE."
            self.data['playSounds'] = ['fear_2']
        return self.data

    def update(self):

        if (self.stage == 0):
            if (self.diry == -1): #going N
                if (self.y > 46):
                    self.data['deltaY'] = '-1'
                else:
                    self.data['deltaY'] = '0'
                    self.data['deltaX'] = '1'
                    self.data['halt'] = '1000000000'
            elif (self.dirx == 1): #going E
                if (self.x < 288):
                    self.data['deltaX'] = '1'
                else:
                    self.data['deltaX'] = '0';
                    self.data['deltaY'] = '1';
                    self.data['halt'] = '1000000000';
            elif (self.diry == 1): #going S
                if (self.y < 174):
                    self.data['deltaY'] = '1'
                else:
                    self.data['deltaY'] = '0'
                    self.data['deltaX'] = '-1'
                    self.data['halt'] = '1000000000';
            elif (self.dirx == -1): #going W
                if (self.x > 33):
                    self.data['deltaX'] = '-1'
                else:
                    self.data['deltaX'] = '0'
                    self.data['deltaY'] = '-1'
                    self.data['halt'] = '1000000000'
        elif(self.stage == 1):
            if (self.x > 49):
                self.data['deltaX'] = '-2'
            elif (self.x < 45):
                self.data['deltaX'] = '2'
            if (self.y > 564):
                self.data['deltaY'] = '-2'
            elif (self.y < 559):
                self.data['deltaY'] = '2'

        return self.data
