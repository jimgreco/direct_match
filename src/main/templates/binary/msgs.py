from Cheetah.Template import Template

class Type:
   def __init__(self, name, length, bytes):
       self.name = name
       self.length = length
       self.getters = []
       self.setters = []
       self.printer = None
       self.bytes = bytes

   def add(self, otype, type, call, name, variable, printer):
       gs = GetSet(type, call, name, variable)
       if otype == 'get':
           self.getters.append(gs)
       if otype == 'set':
           self.setters.append(gs)
       if printer:
           self.printer = gs

class GetSet:
   def __init__(self, type, call, name, variable):
       self.type = type
       self.call = call
       self.name = name
       self.variable = variable

class Enum:
   def __init__(self, name):
       self.name = name
       self.keys = []
 
class Key:
   def __init__(self, name, value):
       self.name = name
       self.value =value

class Msg:
   def __init__(self, name, id):
       self.name = name
       self.nameLower = name[0].lower() + name[1:]
       self.id = id
       self.fields = []

class Field:
   def __init__(self, type, name, offset, length, common):
       self.name = name
       self.offset = offset
       self.length = length
       self.common = common
       self.bytes = type.bytes
       self.getters = type.getters
       self.setters = type.setters
       self.printer = name + type.printer.name
       self.enum = ''

class Constant:
    def __init__(self, name, type, value, final):
        self.name = name
        self.type = type
        self.value = value
        self.final = final

def write(fname, template):
   f = file(fname, 'w')
   f.write(str(template))
   f.close()

def copy(src, dst):
   dst.package = src.package
   dst.msg = src.msg
   dst.inherits = ''
   dst.msgs = src.msgs
   dst.prefix = src.prefix

def createTemplates(names):
    store = []
    for name in names:
       t = Template(file=name + '.java.template')
       t.name = name
       store.append(t)
    return store

def writeTemplate(template, name):
    if 'Test' in template.name:
       dir = template.testDir
    else:
       dir = template.srcDir
    write(dir + '/' + template.prefix + name + template.name + '.java', template)
