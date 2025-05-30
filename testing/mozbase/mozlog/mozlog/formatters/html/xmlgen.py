"""
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

This file is originally from: https://bitbucket.org/hpk42/py, specifically:
https://bitbucket.org/hpk42/py/src/980c8d526463958ee7cae678a7e4e9b054f36b94/py/_xmlgen.py?at=default
by holger krekel, holger at merlinux eu. 2009
"""

import re


def u(s):
    return s


def unicode(x):
    if hasattr(x, "__unicode__"):
        return x.__unicode__()
    return str(x)


class NamespaceMetaclass(type):
    def __getattr__(self, name):
        if name[:1] == "_":
            raise AttributeError(name)
        if self == Namespace:
            raise ValueError("Namespace class is abstract")
        tagspec = self.__tagspec__
        if tagspec is not None and name not in tagspec:
            raise AttributeError(name)
        classattr = {}
        if self.__stickyname__:
            classattr["xmlname"] = name
        cls = type(name, (self.__tagclass__,), classattr)
        setattr(self, name, cls)
        return cls


class Tag(list):
    class Attr:
        def __init__(self, **kwargs):
            self.__dict__.update(kwargs)

    def __init__(self, *args, **kwargs):
        super(Tag, self).__init__(args)
        self.attr = self.Attr(**kwargs)

    def __unicode__(self):
        return self.unicode(indent=0)

    __str__ = __unicode__

    def unicode(self, indent=2):
        l = []
        SimpleUnicodeVisitor(l.append, indent).visit(self)
        return u("").join(l)

    def __repr__(self):
        name = self.__class__.__name__
        return "<%r tag object %d>" % (name, id(self))


Namespace = NamespaceMetaclass(
    "Namespace",
    (object,),
    {
        "__tagspec__": None,
        "__tagclass__": Tag,
        "__stickyname__": False,
    },
)


class HtmlTag(Tag):
    def unicode(self, indent=2):
        l = []
        HtmlVisitor(l.append, indent, shortempty=False).visit(self)
        return u("").join(l)


# exported plain html namespace


class html(Namespace):
    __tagclass__ = HtmlTag
    __stickyname__ = True
    __tagspec__ = dict(
        [
            (x, 1)
            for x in (
                "a,abbr,acronym,address,applet,area,b,bdo,big,blink,"
                "blockquote,body,br,button,caption,center,cite,code,col,"
                "colgroup,comment,dd,del,dfn,dir,div,dl,dt,em,embed,"
                "fieldset,font,form,frameset,h1,h2,h3,h4,h5,h6,head,html,"
                "i,iframe,img,input,ins,kbd,label,legend,li,link,listing,"
                "map,marquee,menu,meta,multicol,nobr,noembed,noframes,"
                "noscript,object,ol,optgroup,option,p,pre,q,s,script,"
                "select,small,span,strike,strong,style,sub,sup,table,"
                "tbody,td,textarea,tfoot,th,thead,title,tr,tt,u,ul,xmp,"
                "base,basefont,frame,hr,isindex,param,samp,var"
            ).split(",")
            if x
        ]
    )

    class Style:
        def __init__(self, **kw):
            for x, y in kw.items():
                x = x.replace("_", "-")
                setattr(self, x, y)


class raw:
    """just a box that can contain a unicode string that will be
    included directly in the output"""

    def __init__(self, uniobj):
        self.uniobj = uniobj


class SimpleUnicodeVisitor:
    """recursive visitor to write unicode."""

    def __init__(self, write, indent=0, curindent=0, shortempty=True):
        self.write = write
        self.cache = {}
        self.visited = {}  # for detection of recursion
        self.indent = indent
        self.curindent = curindent
        self.parents = []
        self.shortempty = shortempty  # short empty tags or not

    def visit(self, node):
        """dispatcher on node's class/bases name."""
        cls = node.__class__
        try:
            visitmethod = self.cache[cls]
        except KeyError:
            for subclass in cls.__mro__:
                visitmethod = getattr(self, subclass.__name__, None)
                if visitmethod is not None:
                    break
            else:
                visitmethod = self.__object
            self.cache[cls] = visitmethod
        visitmethod(node)

    # the default fallback handler is marked private
    # to avoid clashes with the tag name object
    def __object(self, obj):
        # self.write(obj)
        self.write(escape(unicode(obj)))

    def raw(self, obj):
        self.write(obj.uniobj)

    def list(self, obj):
        assert id(obj) not in self.visited
        self.visited[id(obj)] = 1
        for elem in obj:
            self.visit(elem)

    def Tag(self, tag):
        assert id(tag) not in self.visited
        try:
            tag.parent = self.parents[-1]
        except IndexError:
            tag.parent = None
        self.visited[id(tag)] = 1
        tagname = getattr(tag, "xmlname", tag.__class__.__name__)
        if self.curindent and not self._isinline(tagname):
            self.write("\n" + u(" ") * self.curindent)
        if tag:
            self.curindent += self.indent
            self.write(u("<%s%s>") % (tagname, self.attributes(tag)))
            self.parents.append(tag)
            for x in tag:
                self.visit(x)
            self.parents.pop()
            self.write(u("</%s>") % tagname)
            self.curindent -= self.indent
        else:
            nameattr = tagname + self.attributes(tag)
            if self._issingleton(tagname):
                self.write(u("<%s/>") % (nameattr,))
            else:
                self.write(u("<%s></%s>") % (nameattr, tagname))

    def attributes(self, tag):
        # serialize attributes
        attrlist = dir(tag.attr)
        attrlist.sort()
        l = []
        for name in attrlist:
            res = self.repr_attribute(tag.attr, name)
            if res is not None:
                l.append(res)
        l.extend(self.getstyle(tag))
        return u("").join(l)

    def repr_attribute(self, attrs, name):
        if name[:2] != "__":
            value = getattr(attrs, name)
            if name.endswith("_"):
                name = name[:-1]
            if isinstance(value, raw):
                insert = value.uniobj
            else:
                insert = escape(unicode(value))
            return ' %s="%s"' % (name, insert)

    def getstyle(self, tag):
        """return attribute list suitable for styling."""
        try:
            styledict = tag.style.__dict__
        except AttributeError:
            return []
        else:
            stylelist = [x + ": " + y for x, y in styledict.items()]
            return [u(' style="%s"') % u("; ").join(stylelist)]

    def _issingleton(self, tagname):
        """can (and will) be overridden in subclasses"""
        return self.shortempty

    def _isinline(self, tagname):
        """can (and will) be overridden in subclasses"""
        return False


class HtmlVisitor(SimpleUnicodeVisitor):
    single = dict(
        [
            (x, 1)
            for x in ("br,img,area,param,col,hr,meta,link,base," "input,frame").split(
                ","
            )
        ]
    )
    inline = dict(
        [
            (x, 1)
            for x in (
                "a abbr acronym b basefont bdo big br cite code dfn em font "
                "i img input kbd label q s samp select small span strike "
                "strong sub sup textarea tt u var".split(" ")
            )
        ]
    )

    def repr_attribute(self, attrs, name):
        if name == "class_":
            value = getattr(attrs, name)
            if value is None:
                return
        return super(HtmlVisitor, self).repr_attribute(attrs, name)

    def _issingleton(self, tagname):
        return tagname in self.single

    def _isinline(self, tagname):
        return tagname in self.inline


class _escape:
    def __init__(self):
        self.escape = {
            u('"'): u("&quot;"),
            u("<"): u("&lt;"),
            u(">"): u("&gt;"),
            u("&"): u("&amp;"),
            u("'"): u("&apos;"),
        }
        self.charef_rex = re.compile(u("|").join(self.escape.keys()))

    def _replacer(self, match):
        return self.escape[match.group(0)]

    def __call__(self, ustring):
        """xml-escape the given unicode string."""
        ustring = unicode(ustring)
        return self.charef_rex.sub(self._replacer, ustring)


escape = _escape()
