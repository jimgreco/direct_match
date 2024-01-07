import xml.etree.ElementTree as ET
import msgs, sys, string
import shutil, os

root = ET.parse(sys.argv[1]).getroot()
srcDir = sys.argv[2]
testDir = sys.argv[3]

shutil.rmtree(srcDir, ignore_errors=True)
shutil.rmtree(testDir, ignore_errors=True)
os.mkdir(srcDir)
os.mkdir(testDir)

typesXMLFile = root.attrib["types"]
types = {}
typesRoot = ET.parse(root.attrib['types']).getroot()
for el in typesRoot:
  if el.tag == 'type':
     type = msgs.Type(el.attrib['name'], int(el.attrib['length']), el.attrib['bytes'])
     types[type.name] = type
     for key in el:
        name = ''
        variable = False
        printer = False
        if 'name' in key.attrib:
            name = key.attrib['name'] 
        if 'printer' in key.attrib:
            printer = key.attrib['printer'] == 'true'              
        type.add(key.tag, key.attrib['type'], key.attrib['call'], name, variable, printer)

constants = []
enums = []
allMsgs = []
commonTemplates = msgs.createTemplates(['Command', 'Event'])
msgTemplates = msgs.createTemplates(['Listener', 'ByteBufferMessage'])
gtemps = ['Messages', 'TestMessages', 'ByteBufferMessages', 'TestDispatcher', 'BaseDispatcher', 'ByteBufferDispatcher', 'Constants', 'Application', 'CommandSender'];
if 'printer' in root.attrib:
    gtemps.append('Printer')
globalTemplates = msgs.createTemplates(gtemps)

package = root.attrib['package']
prefix = root.attrib['prefix']
timestampOffset = int(root.attrib['timestampOffset']);
msgTypeOffset = int(root.attrib['msgTypeOffset']);
extends = root.attrib['extends'];
templates = commonTemplates + msgTemplates + globalTemplates

commonMsg = msgs.Msg('Common', '.')
commonMsg.length = 0

for t in templates:
  t.package = package
  t.msgs = allMsgs
  t.enums = enums
  t.constants = constants
  t.prefix = prefix
  t.msgTypeOffset = msgTypeOffset
  t.timestampOffset = timestampOffset;
  t.srcDir = srcDir
  t.testDir = testDir

for msg in root:
  if msg.tag == 'enum':
     enum = msgs.Enum(msg.attrib['name'])
     enums.append(enum)
     for key in msg:
         key = msgs.Key(key.attrib['name'], key.attrib['value'])
         enum.keys.append(key) 

  elif msg.tag == 'constant':
      final = False
      if 'final' in msg.attrib:
          final = msg.attrib['final'] == 'true'
      constants.append(msgs.Constant(msg.attrib['name'], msg.attrib['type'], msg.attrib['value'], final))

  elif msg.tag == 'msg':
    name = msg.attrib['name']
    id = msg.attrib['id']

    if id == commonMsg.id:
      for field in msg:
          type = types[field.attrib['type']]
          length = type.length
          if 'length' in field.attrib:
              length = int(field.attrib['length']) 
          fld = msgs.Field(type, field.attrib['name'], commonMsg.length, length, True)
          commonMsg.fields.append(fld)
          commonMsg.length = commonMsg.length + fld.length
      message = commonMsg

    else: 
      message = msgs.Msg(name, id)
      for field in commonMsg.fields:
         message.fields.append(field)
 
      offset = commonMsg.length
      for field in msg:
         type = types[field.attrib['type']]
         length = type.length
         if 'length' in field.attrib:
            length = int(field.attrib['length'])          
         fld = msgs.Field(type, field.attrib['name'], offset, length, False)
         if 'enum' in field.attrib:
             fld.enum = field.attrib['enum']
         message.fields.append(fld)
         offset = offset + fld.length

      message.length = offset
      allMsgs.append(message)

    if 'extends' in msg.attrib:
      message.extends = msg.attrib['extends']
    else:
      message.extends = extends

    for template in commonTemplates:
       template.msg = message
       msgs.writeTemplate(template, message.name)

    if id != commonMsg.id: 
       for template in msgTemplates:
          template.msg = message
          msgs.writeTemplate(template, message.name)
   
for template in globalTemplates:
    msgs.writeTemplate(template, '')

