import xml.etree.ElementTree as ET
from Cheetah.Template import Template
import msgs
import sys

xmlFile = sys.argv[1]
print xmlFile

root = ET.parse(xmlFile).getroot()
package = root.attrib['package']
srcDir = root.attrib['srcDir']
testDir = root.attrib['testDir']
prefix = root.attrib['prefix']

messages = []
tags = []
tTags = Template(file='FixTags.java.template')
tMsgs = Template(file='FixMsgTypes.java.template')
tListener = Template(file='FixListener.java.template')
tDispatcher = Template(file='FixDispatcher.java.template')
tConstants = Template(file='FixConstants.java.template')
tStubTags = Template(file='FixStubTags.java.template')
templates = [tTags, tMsgs, tListener, tDispatcher, tConstants, tStubTags]

for template in templates:
   template.package = package
   template.tags = tags
   template.msgs = messages
   template.prefix = prefix

class Tag:
    def __init__(self, id, name, type):
        self.id = id
        self.name = name
        self.type = type
        self.enums = []

class Msg:
    def __init__(self, name, id, use):
        self.name = name
        self.id = id
        self.input = 'i' in use
        self.output = 'o' in use

class Enum:
    def __init__(self, name, value):
        self.name = name
        self.value = value

for tagEl in root.iter('tag'):
    tag = Tag(tagEl.attrib['id'], tagEl.attrib['name'], tagEl.attrib['type'])   
    for enumEl in tagEl.iter('enum'):
       tag.enums.append(Enum(enumEl.attrib['name'], enumEl.attrib['value']))
    tags.append(tag)

for msgEl in root.iter('msg'):
    msg = Msg(msgEl.attrib['name'], msgEl.attrib['type'], msgEl.attrib['use'])   
    messages.append(msg)

    if msg.input:
       tListener.msg = msg
       msgs.write(srcDir + prefix + msg.name + 'Listener.java', tListener)

msgs.write(srcDir + prefix + 'Tags.java', tTags)
msgs.write(srcDir + prefix + 'MsgTypes.java', tMsgs)
msgs.write(srcDir + prefix + 'Dispatcher.java', tDispatcher)
msgs.write(srcDir + prefix + 'Constants.java', tConstants)
msgs.write(testDir + prefix + 'StubTags.java', tStubTags)
