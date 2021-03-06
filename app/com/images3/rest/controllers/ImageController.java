/*******************************************************************************
 * Copyright 2014 Rui Sun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.images3.rest.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gogoup.dddutils.pagination.PaginatedResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.images3.ImageAddRequest;
import com.images3.ImageResponse;
import com.images3.ImageS3;
import com.images3.common.ImageIdentity;
import com.images3.common.TemplateIdentity;
import com.images3.rest.models.PaginatedResultModel;

import play.mvc.Controller;
import play.mvc.Result;

public class ImageController extends Controller {

    private ImageS3 imageS3;
    private ObjectMapper objectMapper;
    
    @Inject
    public ImageController(ImageS3 imageS3, ObjectMapper objectMapper) {
        this.imageS3 = imageS3;
        this.objectMapper = objectMapper;
    }
    
    public Result addImage(String imagePlantId) throws IOException {
        File imageContent = request().body().asRaw().asFile();
        ImageAddRequest request = new ImageAddRequest(imagePlantId, imageContent);
        ImageResponse response = imageS3.addImage(request);
        String respJson = objectMapper.writeValueAsString(response);
        return ok(respJson);
    }
    
    public Result getImageFile(String imagePlantId, String imageId, String template) throws IOException {
        if (isEmptyTemplate(template)) {
            return getImageFile(imagePlantId, imageId);
        } else {
            return getImageFileWithTemplate(imagePlantId, imageId, template);
        }
    }
    
    private Result getImageFile(String imagePlantId, String imageId) throws IOException {
        File content = imageS3.getImageContent(new ImageIdentity(imagePlantId, imageId));
        return ok(content, content.getName()); 
    }

    private Result getImageFileWithTemplate(String imagePlantId, String imageId, String templateName) throws IOException {
        File content = imageS3.getImageContent(new ImageIdentity(imagePlantId, imageId), templateName);
        return ok(content, content.getName()); 
    }

    public Result getImage(String imagePlantId, String imageId, String template) throws IOException {
        if (isEmptyTemplate(template)) {
            return getImage(imagePlantId, imageId);
        } else {
            return getImageWithTemplate(imagePlantId, imageId, template);
        }
    }
    
    private Result getImage(String imagePlantId, String imageId) throws IOException {
        ImageResponse response = imageS3.getImage(new ImageIdentity(imagePlantId, imageId));
        String respJson = objectMapper.writeValueAsString(response);
        return ok(respJson); 
    }
    
    private Result getImageWithTemplate(String imagePlantId, String imageId, String templateName) throws IOException {
        ImageResponse response = imageS3.getImage(new ImageIdentity(imagePlantId, imageId), templateName);
        String respJson = objectMapper.writeValueAsString(response);
        return ok(respJson); 
    }
    
    public Result getImages(String imagePlantId, String page, String template) throws IOException {
        if (isEmptyTemplate(template)) {
            return getImages(imagePlantId, page);
        } else {
            return getImagesByTemplate(imagePlantId, template, page);
        }
    }
    
    private Result getImages(String imagePlantId, String page) throws IOException {
        PaginatedResult<List<ImageResponse>> pages = imageS3.getImages(imagePlantId);
        return getPaginatedResultResponse(pages, page);
    }
    
    private Result getImagesByTemplate(String imagePlantId, String templateName, String page) throws IOException {
        PaginatedResult<List<ImageResponse>> pages = 
                imageS3.getImages(new TemplateIdentity(imagePlantId, templateName));
        return getPaginatedResultResponse(pages, page);
    }
    
    public Result getVersioningImages(String imagePlantId, String imageId, String page) throws IOException {
        PaginatedResult<List<ImageResponse>> pages = 
                imageS3.getVersioningImages(new ImageIdentity(imagePlantId, imageId));
        return getPaginatedResultResponse(pages, page);
    }
    
    private Result getPaginatedResultResponse(PaginatedResult<List<ImageResponse>> pages, 
            String page) throws IOException {
        if (null == page 
                || page.trim().length() == 0) {
            page = (String) pages.getFirstPageCursor();
        }
        List<ImageResponse> images = pages.getResult(page);
        String nextPage = (String) pages.getNextPageCursor();
        String prevPage = (String) pages.getPrevPageCursor();
        PaginatedResultModel<List<ImageResponse>> response = 
                new PaginatedResultModel<List<ImageResponse>>(prevPage, nextPage, images);
        String respJson = objectMapper.writeValueAsString(response);
        return ok(respJson);
    }
    
    public Result deleteImage(String imagePlantId, String imageId) {
        imageS3.deleteImage(new ImageIdentity(imagePlantId, imageId));
        return status(204);
    }
    
    private boolean isEmptyTemplate(String template) {
        return (null == template || template.trim().length() == 0);
    }
    
}
