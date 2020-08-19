import React, {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useParams, useHistory} from "react-router-dom";
import styled from "@emotion/styled";

import Preview from "../components/editor/Preview";
import MarkdownEditor from "../components/editor/MarkdownEditor";
import FullScreenMode from "../components/common/FullScreenMode";
import SidebarLayout from "../components/common/SidebarLayout";

import slideApi from "../api/slide";
import filesApi from "../api/file";
import {AccessLevel, MOBILE_MAX_WIDTH} from "../domains/constants";
import {clear, saveImg} from "../assets/icons";
import parse, {ParsedData} from "../utils/metadata";
import {googleAnalyticsEvent, googleAnalyticsException, googleAnalyticsPageView} from "../utils/googleAnalytics";

const EditorBlock = styled.main`
  display: flex;
  height: 100%;
  max-height: 100vh;
  
  > div {
    flex: 1;
    position: relative;
    overflow: auto;
  }
  
  @media(max-width: ${MOBILE_MAX_WIDTH}px) {
    flex-direction: column-reverse;
    height: 100vh;
  }
`;

const Edit = styled.div`
  display: flex;
  flex-direction: column;
`;

const ButtonMenu = styled.div`
  display: flex;
  align-items: center;
  padding-left: 15px;
  background-color: #313335;
  
  > button {
    background-position: center;
    background-repeat: no-repeat;
    height: 16px;
    width: 50px;
    margin: 5px;
    border: 0;
    border-radius: 8px;
    outline: none;
    color: #777;
    font-weight: bold;
    background-color: transparent;
    cursor: pointer;
  }
`;

const SaveButton = styled.button`
  background-image: url(${saveImg});
`;

const DeleteButton = styled.button`
  background-image: url(${clear});
`;

interface Params {
  id?: string
}

const Editor: React.FC = () => {
  const params = useParams<Params>();
  const id = parseInt(params?.id ?? "0", 10);
  const history = useHistory();
  const codemirrorRef = useRef<CodeMirror.Editor | null>(null);

  const [content, setContent] = useState<string>("");

  const parsed = useMemo<ParsedData>(() => parse(content), [content]);

  const slides = useMemo<string[]>(() => (
    parsed.content.split(/^---$/m)
      .filter((slideContent: string) => slideContent.trim())
  ), [parsed]);

  useEffect(() => {
    googleAnalyticsPageView("Editor");
  }, []);

  useEffect(() => {
    id && slideApi.get(id)
      .then(({data}) => {
        codemirrorRef.current?.setValue(data.content);
      })
      .catch(() => {
        googleAnalyticsException(`슬라이드 ${id} 불러오기 실패`);
        alert("데이터를 불러오지 못했습니다.");
      });

    if (!id) {
      codemirrorRef.current?.setValue("");
    }
  }, [id]);

  const uploadFile = useCallback((file: File) => new Promise<string>(resolve => {
    filesApi.upload(file)
      .then(response => response.data.urls[0])
      .then(url => resolve(url))
      .catch(() => {
        googleAnalyticsException("파일 업로드 실패");
      });
  }), []);

  const create = useCallback(async () => {
    try {
      const {headers: {location}} = await slideApi.create({
        data: {
          title: parsed.metadata?.title ?? "Untitled",
          content: codemirrorRef.current!.getValue(),
          accessLevel: AccessLevel.PRIVATE,
        },
      });
      const slideId = location.substring(location.lastIndexOf("/") + 1);

      googleAnalyticsEvent("슬라이드", `#${slideId} 저장 완료`);
      history.replace(`/editor/${slideId}`);
    } catch (error) {
      googleAnalyticsException("슬라이드 (신규) 저장 실패");
    }
  }, [history, parsed]);

  const update = useCallback(async () => {
    const data = {
      id,
      data: {
        title: parsed.metadata?.title ?? "Untitled",
        content: codemirrorRef.current!.getValue(),
        accessLevel: AccessLevel.PRIVATE,
      },
    };

    try {
      await slideApi.update(data);
      googleAnalyticsEvent("슬라이드", `#${id} 수정 완료`);
      alert("성공");
    } catch {
      googleAnalyticsException(`슬라이드 #{id} 수정 실패`);
      alert("실패");
    }
  }, [id, parsed]);

  const save = useCallback(() => {
    id ? update() : create();
  }, [id, update, create]);

  const deleteSlide = useCallback(async () => {
    try {
      id && await slideApi.delete(id);
      googleAnalyticsEvent("슬라이드", `#${id} 삭제 완료`);
      history.push("/archive");
    } catch (error) {
      googleAnalyticsException(`슬라이드 #${id} 삭제 실패`);
    }
  }, [history, id]);

  return (
    <SidebarLayout fluid>
      <EditorBlock>
        <Edit>
          <ButtonMenu>
            <SaveButton onClick={save}/>
            <DeleteButton onClick={deleteSlide}/>
          </ButtonMenu>
          <MarkdownEditor inputRef={codemirrorRef} onChange={setContent} onDrop={uploadFile}
            onSaveKeyDown={save}/>
          <FullScreenMode contents={slides}/>
        </Edit>
        <Preview content={content}/>
      </EditorBlock>
    </SidebarLayout>
  );
};

export default Editor;
